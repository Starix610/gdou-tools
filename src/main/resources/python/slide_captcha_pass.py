# -*- coding: utf-8 -*-
import json
import re
import time
from urllib.parse import urlencode
import cv2
import numpy
import requests
import execjs


class CaptchaProcessor(object):
    def __init__(self):
        self.session = requests.session()
        self.rtk = ''

    def get_img_url(self):
        # 获取JS中的rtk参数
        url = 'https://jw.gdou.edu.cn/zfcaptchaLogin'
        params = {
            'type': 'resource',
            'name': 'zfdun_captcha.js',
            'instanceId': 'zfcaptchaLogin'
        }
        resp = self.session.get(url, params=params)
        rtk = re.search("rtk:'(.*)',", resp.text).group(1)
        self.rtk = rtk
        # 获取图片url
        params = {
            'type': 'refresh',
            'rtk': rtk,
            'time': int(round(time.time() * 1000)),
            'instanceId': 'zfcaptchaLogin'
        }
        resp = self.session.get(url, params=params)
        json = resp.json()
        img_params = {
            'type': 'image',
            'id': json['mi'],
            'imtk': json['imtk'],
            't': int(round(time.time() * 1000)),
            'instanceId': 'zfcaptchaLogin'
        }
        slide_url = '%s?%s' % (url, urlencode(img_params))
        img_params['id'] = json['si']
        back_url = '%s?%s' % (url, urlencode(img_params))
        return slide_url, back_url

    def get_img_bytes(self, url):
        resp = self.session.get(url)
        return resp.content

    # 清除滑块图片空白区域
    @staticmethod
    def clear_white(img):
        rows, cols, channel = img.shape
        min_x = 255
        min_y = 255
        max_x = 0
        max_y = 0
        for x in range(1, rows):
            for y in range(1, cols):
                t = set(img[x, y])
                if len(t) >= 2:
                    if x <= min_x:
                        min_x = x
                    elif x >= max_x:
                        max_x = x

                    if y <= min_y:
                        min_y = y
                    elif y >= max_y:
                        max_y = y
        img1 = img[min_x:max_x, min_y: max_y]
        return img1

    # 边缘检测
    @staticmethod
    def image_edge_detection(img):
        edges = cv2.Canny(img, 100, 200)
        return edges

    # template match
    def template_match(self, tpl, target):
        result = cv2.matchTemplate(target, tpl, cv2.TM_CCOEFF_NORMED)
        # 寻找矩阵(一维数组当作向量,用Mat定义) 中最小匹配概率和最大匹配概率的位置
        min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(result)
        tl = max_loc
        # 输出横坐标, 即 滑块缺口在图片上的位置
        return tl[0]

    def detect_distance(self):
        slide_url, back_url = self.get_img_url()
        # 滑块图片
        slide_bytes = self.get_img_bytes(slide_url)
        slide = cv2.imdecode(numpy.frombuffer(slide_bytes, numpy.uint8), cv2.IMREAD_COLOR)
        slide = self.clear_white(slide)
        slide = cv2.cvtColor(slide, cv2.COLOR_RGB2GRAY)
        slide = self.image_edge_detection(slide)

        # 背景图片
        back_bytes = self.get_img_bytes(back_url)
        back = cv2.imdecode(numpy.frombuffer(back_bytes, numpy.uint8), cv2.IMREAD_GRAYSCALE)
        back = self.image_edge_detection(back)

        slide_pic = cv2.cvtColor(slide, cv2.COLOR_GRAY2RGB)
        back_pic = cv2.cvtColor(back, cv2.COLOR_GRAY2RGB)
        x = self.template_match(slide_pic, back_pic)
        return x

    # 移动轨迹生成
    def movement_track_generate(self, distance):
        v = 0
        t = 8
        now = int(round(time.time() * 1000))
        start_time = now
        track = []
        current = 0
        threshold = distance * 2 / 5  # 减速距离阀值
        while current < distance:
            if current < threshold:
                a = 0.01  # 低于减速阈值，加速度为0.01
            else:
                a = -0.007  # 达到减速阈值，加速度为-0.007
            s = v * t + 0.5 * a * (t ** 2)
            v = v + a * t
            current += round(s)
            location = {
                # 一般来说不会从屏幕0位置开始滑，所以统一加800，让轨迹更接近真实
                'x': current + 800,
                # 服务端只校验x坐标，y坐标可以任意
                'y': 483,
                't': now,
            }
            track.append(location)
            # 每8ms一个记录
            now += 8

        # 轨迹参数加密，调原JS代码中的加密方法
        encoded_track = execjs.compile(open('encode.js').read()).call('ef', json.dumps(track))
        return encoded_track

    # 提交验证，返回验证成功后的Cookie
    def submit(self, track):
        url = 'https://jw.gdou.edu.cn/zfcaptchaLogin'
        params = {
            'type': 'verify',
            'rtk': self.rtk,
            'time': int(round(time.time() * 1000)),
            'mt': track,
            'instanceId': 'zfcaptchaLogin',
            'extend': 'eyJhcHBOYW1lIjoiTmV0c2NhcGUiLCJ1c2VyQWdlbnQiOiJNb3ppbGxhLzUuMCAoV2luZG93cyBOVCAxMC4wOyBXaW42NDsgeDY0KSBBcHBsZVdlYktpdC81MzcuMzYgKEtIVE1MLCBsaWtlIEdlY2tvKSBDaHJvbWUvODguMC40MzI0LjEwNCBTYWZhcmkvNTM3LjM2IiwiYXBwVmVyc2lvbiI6IjUuMCAoV2luZG93cyBOVCAxMC4wOyBXaW42NDsgeDY0KSBBcHBsZVdlYktpdC81MzcuMzYgKEtIVE1MLCBsaWtlIEdlY2tvKSBDaHJvbWUvODguMC40MzI0LjEwNCBTYWZhcmkvNTM3LjM2In0=',
        }
        resp = self.session.post(url, data=params)
        return resp.json()['status']


if __name__ == "__main__":
    p = CaptchaProcessor()
    # 识别失败最大重试次数
    retry = 5
    while True:
        x = p.detect_distance()
        track = p.movement_track_generate(x)
        status = p.submit(track)
        if status == 'success':
            # 返回cookie
            for cookie in p.session.cookies:
                print(cookie.name + "=" + cookie.value)
            break
        if retry <= 0:
            exit(1)
        retry -= 1
