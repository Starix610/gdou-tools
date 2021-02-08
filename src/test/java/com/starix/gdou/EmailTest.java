package com.starix.gdou;

import com.jfinal.kit.Kv;
import com.starix.gdou.dto.ScoreNotifyDTO;
import com.starix.gdou.utils.HtmlMailRenderUtil;
import com.starix.gdou.utils.MailUtil;
import com.starix.gdou.utils.SslUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Starix
 * @date 2020-07-18 12:20
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class EmailTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    public void testSend() throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        messageHelper.setFrom("在思考的猫<starix610@163.com>");
        messageHelper.setTo("646722505@qq.com");
        messageHelper.setSubject("成绩更新通知");
        String html = "<h1>这是html</h1>";
        messageHelper.setText(html, true);
        SslUtil.ignoreSsl();
        javaMailSender.send(mimeMessage);
    }

    @Autowired
    private MailUtil mailUtil;

    @Test
    public void testSendMailUtil() throws Exception {
        List<ScoreNotifyDTO> notifyDTOList = new ArrayList<>();
        ScoreNotifyDTO notifyDTO = ScoreNotifyDTO.builder()
                .courseName("计算机工程伦理与工程管理")
                .credit("2")
                .property("必修")
                .score("86")
                .isNew(false)
                .build();
        notifyDTOList.add(notifyDTO);
        notifyDTO = ScoreNotifyDTO.builder()
                .courseName("形式与政策教育3")
                .credit("0.5")
                .property("必修")
                .score("85")
                .isNew(false)
                .build();
        notifyDTOList.add(notifyDTO);
        notifyDTO = ScoreNotifyDTO.builder()
                .courseName("(网络课)转基因的科学——基因工程")
                .credit("1.5")
                .property("任选")
                .score("83")
                .isNew(false)
                .build();
        notifyDTOList.add(notifyDTO);
        notifyDTO = ScoreNotifyDTO.builder()
                .courseName("创新创业教育2")
                .credit("0.5")
                .property("必修")
                .score("95")
                .isNew(true)
                .build();
        notifyDTOList.add(notifyDTO);
        Kv data = Kv.create()
                .set("scoreList", notifyDTOList)
                .set("username", "201711xxxxxx")
                .set("year", "2019-2020")
                .set("semester", "2")
                .set("queryMore", "https://www.starix.top:9800/score.html");
        String html = HtmlMailRenderUtil.render("templates/score-email-template.html", data);
        mailUtil.sendHtmlMail("646722505@qq.com", "成绩更新通知", html);
    }

    @Test
    public void testException(){
        try {
            int a = 2/0;
        } catch (Exception e){
            log.error("[{}]错误", "测试", e);
        }
    }

}
