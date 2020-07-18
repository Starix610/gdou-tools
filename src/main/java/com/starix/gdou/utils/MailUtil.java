package com.starix.gdou.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * @author Starix
 * @date 2020-07-18 16:26
 */
@Component
public class MailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendMail(String receiver, String subject, String content) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setFrom("在思考的猫<starix610@163.com>");
        messageHelper.setTo(receiver);
        messageHelper.setSubject(subject);
        messageHelper.setText(content, false);
        SslUtil.ignoreSsl();
        javaMailSender.send(mimeMessage);
    }

    public void sendHtmlMail(String receiver, String subject, String html) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setFrom("在思考的猫<starix610@163.com>");
        messageHelper.setTo(receiver);
        messageHelper.setSubject(subject);
        messageHelper.setText(html, true);
        SslUtil.ignoreSsl();
        javaMailSender.send(mimeMessage);
    }

}
