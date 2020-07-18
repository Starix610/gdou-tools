package com.starix.gdou.service.impl;

import com.starix.gdou.common.Constant;
import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.dto.response.YearOptionListResponseDTO;
import com.starix.gdou.entity.Student;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.repository.StudentRepository;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.GdouJWScoreNotifyService;
import com.starix.gdou.service.GdouJWServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Starix
 * @date 2020-07-17 21:59
 */
@Service
@Slf4j
public class GdouJWScoreNotifyServiceImpl implements GdouJWScoreNotifyService{

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GdouJWServiceV2 gdouJWService;

    @Override
    public void enableNotify(String openid, String email) throws Exception {
        Student student = studentRepository.findByOpenid(openid);
        if (student == null){
            throw new CustomException(CommonResult.failed("未绑定学号"));
        }
        if (student.getNotifyStatus() == 1){
            throw new CustomException(CommonResult.failed("你已经开启过成绩通知了"));
        }
        LoginResultV2 loginResult = gdouJWService.login(student.getUsername(), student.getPassword());
        YearOptionListResponseDTO yearOptionList = gdouJWService.getSocreYearOptionList(loginResult.getCookie());
        ScoreQueryRquestDTO scoreQueryRquestDTO = ScoreQueryRquestDTO.builder()
                .cookie(loginResult.getCookie())
                .year(yearOptionList.getYearValueList().get(yearOptionList.getSelectedYear()))
                .semester(yearOptionList.getSemesterValueList().get(yearOptionList.getSelectedSemester()))
                .build();
        List<ScoreQueryResponseDTO> scoreList = gdouJWService.queryScore(scoreQueryRquestDTO);

        //查询当前成绩并存入redis，下次查询做对比
        redisTemplate.opsForValue().set(Constant.SCORE_NOTIFY_REDIS_KEY_PREFIX + loginResult.getUsername(), scoreList);

        //更新用户状态
        student.setEmail(email);
        student.setNotifyStatus(1);
        studentRepository.save(student);
        log.info("[{}]开启成绩更新通知成功", student.getUsername());
    }

    @Override
    public void disableNotify(String openid) {
        Student student = studentRepository.findByOpenid(openid);
        if (student == null){
            throw new CustomException(CommonResult.failed("未绑定学号"));
        }
        redisTemplate.delete(Constant.SCORE_NOTIFY_REDIS_KEY_PREFIX + student.getUsername());
        student.setNotifyStatus(0);
        studentRepository.save(student);
        log.info("[{}]关闭成绩更新通知成功", student.getUsername());

    }
}
