import smtplib
import time

from email.mime.text import MIMEText

# 发送方邮箱
msg_from = 'starix610@163.com'
# 发送方邮箱授权码
password = 'xxxx'


def send_email(msg_to, subject, content):
    subject = subject  # 主题
    content = content
    # 生成一个MIMEText对象（还有一些其它参数）
    msg = MIMEText(content)
    # 放入邮件主题
    msg['Subject'] = subject
    # 也可以这样传参
    # msg['Subject'] = Header(subject, 'utf-8')
    # 放入发件人
    msg['From'] = msg_from
    try:
        # 通过ssl方式发送，服务器地址，端口
        s = smtplib.SMTP_SSL("smtp.163.com", 465)
        # 登录到邮箱
        s.login(msg_from, password)
        # 发送邮件：发送方，收件方，要发送的消息
        s.sendmail(msg_from, msg_to, msg.as_string())
        print('result-邮件发送成功')
    except s.SMTPException as e:
        print('result-邮件发送失败:%s' % e)
    finally:
        s.quit()
