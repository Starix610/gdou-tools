import argparse
import base64
import requests
import uuid
from pyDes import *
import cpdaily_auth
import email_util

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                  'Chrome/79.0.3945.130 Safari/537.36'
    # 'Cookie': '',
    # 'Cpdaily-Extension': ''
}

# 读取命令行参数
ap = argparse.ArgumentParser()
ap.add_argument("-u", "--username", required=True, help="学号")
ap.add_argument("-p", "--password", required=True, help="密码")
ap.add_argument("-lon", "--longitude", required=True, help="经度")
ap.add_argument("-lat", "--latitude", required=True, help="纬度")
ap.add_argument("-reason", "--abnormalReason", required=True, help="签到内容")
ap.add_argument("-pos", "--position", required=True, help="地理位置")
ap.add_argument("-m", "--email", required=True, help="接收通知的邮箱")
args = vars(ap.parse_args())
username = args['username']
password = args['password']
longitude = args['longitude']
latitude = args['latitude']
abnormalReason = args['abnormalReason']
position = args['position']
email = args['email']


# 执行登录，获取认证信息
def authentication():
    # MOD_AUTH_CAS是最关键认证字段
    mod_auth_cas = cpdaily_auth.do_auth(username, password)
    headers['Cookie'] = 'MOD_AUTH_CAS=%s' % mod_auth_cas


# 获取签到信息
def get_stu_sign_info():
    try:
        response = requests.post('https://gdou.cpdaily.com/wec-counselor-sign-apps/stu/sign/getStuSignInfosInOneDay',
                                 json={}, headers=headers)
    except requests.exceptions.SSLError as e:
        print('result-获取信息失败')
        email_util.send_email(email, '签到失败', '签到失败：获取信息失败')
        exit(0)
    json = response.json()
    if json['datas']['signedTasks']:
        print('result-今日已签到，不再重复提交签到')
        email_util.send_email(email, '签到失败', '签到失败：今日您已完成签到，不再重复提交签到')
        exit(0)
    elif json['datas']['unSignedTasks']:
        return json['datas']['unSignedTasks'][0]['signInstanceWid']
    else:
        email_util.send_email(email, '签到失败', '签到失败：未获取到签到信息')
        exit(0)


# DES加密
def encrypt(s, key='ST83=@XV'):
    key = key
    iv = b"\x01\x02\x03\x04\x05\x06\x07\x08"
    k = des(key, CBC, iv, pad=None, padmode=PAD_PKCS5)
    encrypt_str = k.encrypt(s)
    return base64.b64encode(encrypt_str).decode()


# 生成Cpdaily-Extension关键签到参数
def create_cpdaily_extension(lon, lat, uid):
    """
     headers中的CpdailyInfo参数
     :param lon: 定位经度
     :param lat: 定位纬度
     :param uid: 学生学号
     :return: Cpdaily-Extension
    """
    s = r'{"systemName":"android","systemVersion":"8.1.0","model":"16th",' \
        r'"deviceId":"' + str(uuid.uuid1()) + '","appVersion":"8.1.11","lon":' \
        + str(lon) + ',"lat":' + str(lat) + ',"userId":"' + str(uid) + '"}'
    extension = encrypt(s)
    return extension


# 执行签到
def submit_sign():
    submit_url = 'https://gdou.cpdaily.com/wec-counselor-sign-apps/stu/sign/submitSign'
    wid = get_stu_sign_info()
    data = {
        "signInstanceWid": wid,
        "longitude": longitude,
        "latitude": latitude,
        "isMalposition": 1,
        "abnormalReason": abnormalReason,
        "signPhotoUrl": "",
        "position": position
    }
    headers['Cpdaily-Extension'] = create_cpdaily_extension(data['longitude'], data['latitude'], username)
    response = requests.post(submit_url, json=data, headers=headers)
    if response.json()['message'] == 'SUCCESS':
        print('result-签到成功')
        email_util.send_email(email, '签到成功', '今日签到已完成！')
    else:
        print('result-%s' % response.json())
        email_util.send_email(email, '签到失败', '签到失败，详细信息：%s' % response.json()['message'])


if __name__ == '__main__':
    authentication()
    submit_sign()
