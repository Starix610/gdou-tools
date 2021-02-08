package com.starix.gdou.service;

import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ExamQueryRquestDTO;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ExamQueryResponseDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.dto.response.YearOptionListResponseDTO;
import org.apache.http.cookie.Cookie;

import java.io.IOException;
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
    LoginResultV2 login(String useranme, String password) throws Exception;


    /**
     * 通过openid查询已绑定的学号信息实现自动登录
     * @param openid
     * @return
     */
    LoginResultV2 loginByOpenid(String openid) throws Exception;

    /**
     * 查询成绩信息
     * @param scoreQueryRquestDTO
     * @return
     */
    List<ScoreQueryResponseDTO> queryScore(ScoreQueryRquestDTO scoreQueryRquestDTO) throws IOException;

    /**
     * 查询考试信息
     * @param examQueryRquestDTO
     * @return
     */
    ExamQueryResponseDTO queryExam(ExamQueryRquestDTO examQueryRquestDTO);

    /**
     * 获得查成绩页面年份下拉列表数据
     * @param cookies
     * @return
     */
    YearOptionListResponseDTO getSocreYearOptionList(List<Cookie> cookies) throws Exception;

    /**
     * 获得查考试页面年份下拉列表数据
     * @return
     */
    List<String> getExamYearOptionList(List<Cookie> cookies) throws Exception;

}