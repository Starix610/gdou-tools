import requests
from bs4 import BeautifulSoup
import cpdaily_captcha_ocr as ocr
import argparse
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                  'Chrome/79.0.3945.130 Safari/537.36',
}


def get_argument():
    # 读取命令行参数
    ap = argparse.ArgumentParser()
    ap.add_argument("-u", "--username", required=True, help="学号")
    ap.add_argument("-p", "--password", required=True, help="密码")
    args = vars(ap.parse_args())
    return args['username'], args['password']


def do_auth(username, password):
    session = requests.session()
    auth_url = 'https://authserver.gdou.edu.cn/authserver/login?service=https%3A%2F%2Fgdou.cpdaily.com%2Fwec-counselor' \
               '-sign-apps%2Fstu%2Fsign%2FgetStuSignInfosInOneDay'
    response = session.get(auth_url, headers=headers, verify=False)
    soup = BeautifulSoup(response.text, 'html.parser')
    lt = soup.find(attrs={'name': 'lt'})["value"]
    execution = soup.find(attrs={'name': 'execution'})["value"]
    captcha_url = 'http://authserver.gdou.edu.cn/authserver/captcha.html'
    while True:
        response = session.get(captcha_url, headers=headers)
        captcha = ocr.get_code(response.content)
        data = {
            "username": username,
            "password": password,
            "captchaResponse": captcha,
            "lt": lt,
            "execution": execution,
            "_eventId": "submit",
            "rmShown": 1
        }
        response = session.post(auth_url, data=data, headers=headers)
        if '密码有误' in response.text:
            print('result-认证失败')
            return None
        elif '无效的验证码' in response.text:
            print('验证码错误，正在重试')
        else:
            # 经测试发现有时候Cookie中MOD_AUTH_CAS会是一个空值，需要重试获取
            if 'MOD_AUTH_CAS' not in session.cookies:
                print('MOD_AUTH_CAS为空，正在重试')
                continue
            print('result-认证成功')
            return session.cookies['MOD_AUTH_CAS']


if __name__ == '__main__':
    do_auth(get_argument()[0], get_argument()[1])