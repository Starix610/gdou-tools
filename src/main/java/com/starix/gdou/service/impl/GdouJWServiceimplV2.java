package com.starix.gdou.service.impl;

import com.starix.gdou.pojo.LoginResult;
import com.starix.gdou.service.GdouJWService;
import com.starix.gdou.vo.ExamVO;
import com.starix.gdou.vo.ScoreVO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 适配新版教务系统的service实现类
 * @author shiwenjie
 * @created 2020/6/28 5:16 下午
 **/
@Service
public class GdouJWServiceimplV2 implements GdouJWService {

    @Override
    public LoginResult login(String xh, String password) throws Exception {
        return null;
    }

    @Override
    public List<ScoreVO> getScore(LoginResult loginResult, String year, String semester) throws IOException {
        return null;
    }

    @Override
    public List<ExamVO> getExam(LoginResult loginResult, String year, String semester) throws IOException, Exception {
        return null;
    }

    @Override
    public List<String> getSocreYearOptionsList(LoginResult loginResult) throws Exception {
        return null;
    }

    @Override
    public List<String> getExamYearOptionsList(LoginResult loginResult) throws Exception {
        return null;
    }

    @Override
    public LoginResult loginByOpenid(String openid) throws Exception {
        return null;
    }

    @Override
    public void autoEvaluate(LoginResult loginResult, String content, Integer mode) throws Exception {

    }
}