package com.starix.scorequery.service;

import com.starix.scorequery.pojo.LoginResult;
import com.starix.scorequery.vo.ScoreVO;

import java.io.IOException;
import java.util.List;

/**
 * @author Tobu
 * @date 2019-11-20 20:05
 */
public interface SpiderService {


    /**
     * 执行登录
     * @param xh 学号
     * @param password 密码
     * @return LoginResult 封装登录成功之后的cookie值等关键数据，用于维持登录
     */
    LoginResult login(String xh, String password) throws Exception;


    /**
     * 抓取成绩
     * @param loginResult 登录成功之后的必要信息
     * @param year 学年
     * @param semester 学期
     * @return
     */
    List<ScoreVO> getScore(LoginResult loginResult, String year, String semester) throws IOException;


    /**
     * 获得年份下拉列表数据
     * @param loginResult
     * @return
     */
    List<String> getYearOptionsList(LoginResult loginResult) throws IOException, Exception;
}
