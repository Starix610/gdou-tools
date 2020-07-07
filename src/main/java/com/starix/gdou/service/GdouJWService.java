package com.starix.gdou.service;

import com.starix.gdou.dto.LoginResult;
import com.starix.gdou.vo.ExamVO;
import com.starix.gdou.vo.ScoreVO;

import java.io.IOException;
import java.util.List;

/**
 * 教务系统服务service
 * v1版本
 * @author Starix
 * @date 2019-11-20 20:05
 */
public interface GdouJWService {


    /**
     * 执行登录
     * @param xh 学号
     * @param password 密码
     * @return LoginResult 封装登录成功之后的cookie值等关键数据，用于维持登录
     */
    LoginResult login(String xh, String password) throws Exception;


    /**
     * 抓取成绩信息
     * @param loginResult 登录成功之后的必要信息
     * @param year 学年
     * @param semester 学期
     * @return
     */
    List<ScoreVO> getScore(LoginResult loginResult, String year, String semester) throws IOException;


    /**
     * 抓取考试信息
     * @param loginResult
     * @param year
     * @param semester
     * @return
     */
    List<ExamVO> getExam(LoginResult loginResult, String year, String semester) throws Exception;

    /**
     * 获得查成绩页面年份下拉列表数据
     * @param loginResult
     * @return
     */
    List<String> getSocreYearOptionsList(LoginResult loginResult) throws Exception;


    /**
     * 获得查考试页面年份下拉列表数据
     * @param loginResult
     * @return
     */
    List<String> getExamYearOptionsList(LoginResult loginResult) throws Exception;


    /**
     * 通过openid查询已绑定的学号信息实现自动登录
     * @param openid
     * @return
     * @throws Exception
     */
    LoginResult loginByOpenid(String openid) throws Exception;


    /**
     * 一键自动评教
     * @param mode 一键评教模式：0->全程自动，1->自动填写，官网手动修改提交
     * @throws Exception
     */
    void autoEvaluate(LoginResult loginResult, String content, Integer mode) throws Exception;
}
