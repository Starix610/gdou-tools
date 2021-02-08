package com.starix.gdou.service.impl;

import com.starix.gdou.common.Constant;
import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.entity.Student;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.repository.StudentRepository;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.GdouJWScoreNotifyService;
import com.starix.gdou.service.GdouJWServiceV2;
import com.starix.gdou.utils.WxMessagePushUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.starix.gdou.common.Constant.WX_PUSH_TOKEN_MAIN_LOG;

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
    private StringRedisTemplate stringRedisTemplate;
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
        // YearOptionListResponseDTO yearOptionList = gdouJWService.getSocreYearOptionList(loginResult.getCookies());
        // 成绩通知使用自定义的年份学期，不使用官网当前默认的年份学期
        String yearSemester = stringRedisTemplate.opsForValue().get("notify:year_semester");
        String selectedYear = yearSemester.split("-")[0];
        String selectedSemester = yearSemester.split("-")[1];
        ScoreQueryRquestDTO scoreQueryRquestDTO = ScoreQueryRquestDTO.builder()
                .cookies(loginResult.getCookies())
                .year(selectedYear)
                .semester(selectedSemester)
                .build();
        List<ScoreQueryResponseDTO> scoreList = gdouJWService.queryScore(scoreQueryRquestDTO);

        //查询当前成绩并存入redis，下次查询做对比
        redisTemplate.opsForValue().set(Constant.SCORE_NOTIFY_REDIS_KEY_PREFIX + loginResult.getUsername(), scoreList);

        //更新用户状态
        student.setEmail(email);
        student.setNotifyStatus(1);
        studentRepository.save(student);
        log.info("开启成绩更新通知成功, openid: {}, email: {}, username: {}", openid, email, student.getUsername());
        WxMessagePushUtil.push(WX_PUSH_TOKEN_MAIN_LOG,
                String.format("开启成绩更新通知成功, openid: %s, email: %s, username: %s",
                        openid, email, student.getUsername()));
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

    @Override
    public Map<String, Object> queryNotifyStatus(String openid) {
        Student student = studentRepository.findByOpenid(openid);
        if (student == null){
            throw new CustomException(CommonResult.failed("未绑定学号"));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", student.getNotifyStatus());
        map.put("email", student.getEmail());
        return map;
    }

    @Override
    public String queryNotifyYearSemester() {
        return (String) redisTemplate.opsForValue().get("notify:year_semester");
    }
}
