package com.starix.gdou.task;

import com.jfinal.kit.Kv;
import com.starix.gdou.common.Constant;
import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.ScoreNotifyDTO;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.dto.response.YearOptionListResponseDTO;
import com.starix.gdou.entity.Student;
import com.starix.gdou.repository.StudentRepository;
import com.starix.gdou.service.GdouJWServiceV2;
import com.starix.gdou.utils.HtmlMailRenderUtil;
import com.starix.gdou.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Starix
 * @date 2020-07-18 12:13
 */
@Slf4j
@Component
public class ScoreNotifyAsyncTask {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private GdouJWServiceV2 gdouJWService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MailUtil mailUtil;

    @Async
    public Future<String> checkScoreUpdateAndNotify() throws Exception {
        List<Student> studentList = studentRepository.findAllByNotifyStatus(1);
        for (Student student : studentList) {
            LoginResultV2 loginResult = gdouJWService.login(student.getUsername(), student.getPassword());
            YearOptionListResponseDTO yearOptionList = gdouJWService.getSocreYearOptionList(loginResult.getCookie());
            ScoreQueryRquestDTO scoreQueryRquestDTO = ScoreQueryRquestDTO.builder()
                    .cookie(loginResult.getCookie())
                    .year(yearOptionList.getYearValueList().get(yearOptionList.getSelectedYear()))
                    .semester(yearOptionList.getSemesterValueList().get(yearOptionList.getSelectedSemester()))
                    .build();
            //当前成绩
            List<ScoreQueryResponseDTO> currentScoreDTOList = gdouJWService.queryScore(scoreQueryRquestDTO);
            //上一次查询的成绩
            List<ScoreQueryResponseDTO> oldScoreDTOList = (List<ScoreQueryResponseDTO>) redisTemplate.opsForValue()
                    .get(Constant.SCORE_NOTIFY_REDIS_KEY_PREFIX + student.getUsername());
            if (oldScoreDTOList.size() != currentScoreDTOList.size()){
                List<ScoreNotifyDTO> scoreNotifyDTOList = buildScoreNotifyDTOList(student.getUsername(), currentScoreDTOList, oldScoreDTOList);
                Kv emailData = Kv.create()
                        .set("scoreList", scoreNotifyDTOList)
                        .set("username", student.getUsername())
                        .set("year", yearOptionList.getYearTextList().get(yearOptionList.getSelectedYear()))
                        .set("semester", yearOptionList.getSemesterTextList().get(yearOptionList.getSelectedSemester()))
                        .set("queryMore", Constant.SCORE_QUERY_URL + "?openid=" + student.getOpenid());
                //发送邮件
                sendNotifyEmail(student.getUsername(), student.getEmail(), emailData);
            }else {
                log.info("[{}]成绩未更新", student.getUsername());
            }
        }
        return new AsyncResult<>("任务执行完毕");
    }


    private List<ScoreNotifyDTO> buildScoreNotifyDTOList(String username, List<ScoreQueryResponseDTO> currentScoreDTOList, List<ScoreQueryResponseDTO> oldScoreDTOList){
        List<ScoreNotifyDTO> scoreNotifyDTOList = new ArrayList<>();
        for (ScoreQueryResponseDTO currentScoreDTO : currentScoreDTOList) {
            ScoreNotifyDTO scoreNotifyDTO = new ScoreNotifyDTO();
            BeanUtils.copyProperties(currentScoreDTO, scoreNotifyDTO);
            if (!oldScoreDTOList.contains(currentScoreDTO)){
                log.info("[{}]成绩更新-{}", username, currentScoreDTO.getCourseName());
                scoreNotifyDTO.setNew(true);
            }
            scoreNotifyDTOList.add(scoreNotifyDTO);
        }
        //更新redis
        redisTemplate.opsForValue().set(Constant.SCORE_NOTIFY_REDIS_KEY_PREFIX + username, currentScoreDTOList);
        return scoreNotifyDTOList;
    }

    private void sendNotifyEmail(String username, String email, Kv emailData){
        String html = HtmlMailRenderUtil.render("templates/score-email-template.html", emailData);
        try {
            mailUtil.sendHtmlMail(email, "成绩更新通知", html);
            log.info("[{}]发送通知邮件给{}成功", username, email);
        } catch (Exception e) {
            log.error("[{}]发送通知邮件给{}失败", username, email, e);
        }
    }
}
