import requests
from school_api.check_code import CHECK_CODE


def getCode():
    response = requests.get('http://210.38.137.126:8016/CheckCode.aspx')
    code = CHECK_CODE.verify(response.content)
    cookies = requests.utils.dict_from_cookiejar(response.cookies)
    # 输出code和cookie，给Java使用
    print(code)
    print(cookies['ASP.NET_SessionId'])


if __name__ == '__main__':
    getCode()