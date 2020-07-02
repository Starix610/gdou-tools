package com.starix.gdou.service;

import com.starix.gdou.dto.request.ExamQueryRquestDTO;
import com.starix.gdou.dto.response.ExamQueryResponseDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;

import java.util.List;

/**
 * 教务系统服务service
 * v2版本
 * 由于v1的service接口不太通用，因此直接构建v2版本的新sercice接口
 * @author shiwenjie
 * @created 2020/7/1 2:23 下午
 **/
public interface GdouJWServiceV2 {


    /**
     * 执行登录，返回登录成功的cookie，用于维持登录状态，后续进行查询无需再次登录
     * @param useranme 学号
     * @param password 密码
     * @return
     */
    String login(String useranme, String password);


    /**
     * 通过openid查询已绑定的学号信息实现自动登录
     * @param openid
     * @return
     */
    String loginByOpenid(String openid);

    /**
     * 查询成绩信息
     * @param scoreQueryResponseDTO
     * @return
     */
    ScoreQueryResponseDTO queryScore(ScoreQueryResponseDTO scoreQueryResponseDTO);

    /**
     * 查询考试信息
     * @param examQueryRquestDTO
     * @return
     */
    ExamQueryResponseDTO queryExam(ExamQueryRquestDTO examQueryRquestDTO);

    /**
     * 获得查成绩页面年份下拉列表数据
     * @param cookie
     * @return
     */
    List<String> getSocreYearOptionsList(String cookie) throws Exception;

    /**
     * 获得查考试页面年份下拉列表数据
     * @return
     */
    List<String> getExamYearOptionsList(String cookie) throws Exception;

}