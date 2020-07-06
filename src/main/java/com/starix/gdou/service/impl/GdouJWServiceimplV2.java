package com.starix.gdou.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ExamQueryRquestDTO;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ExamQueryResponseDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.GdouJWServiceV2;
import com.starix.gdou.utils.HttpClientUtil;
import com.starix.gdou.utils.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 适配新版教务系统的service实现类
 * @author shiwenjie
 * @created 2020/6/28 5:16 下午
 **/
@Service
@Slf4j
public class GdouJWServiceimplV2 implements GdouJWServiceV2 {

    //教务系统地址
    @Value("${gdou.webvpn-url}")
    private String WEBVPN_URL;
    @Value("${gdou.jw-url}")
    private String JW_URL;
    @Value("${gdou.webvpn-username}")
    private String WEBVPN_USERNAME;
    @Value("${gdou.webvpn-password}")
    private String WEBVPN_PASSWORD;



    @Override
    public LoginResultV2 login(String useranme, String password) throws Exception {
        HttpClientUtil httpClient = new HttpClientUtil();
        // 一次完整流程中的所有请求都需要使用同一个httpclient实例，保持cookie同步
        doWebvpnLogin(httpClient);
        // 获取publickey，用于密码rsa加密
        String resultStr = httpClient.doGet(JW_URL + "/xtgl/login_getPublicKey.html");
        JSONObject json = JSON.parseObject(resultStr);
        String modulus = json.getString("modulus");
        String exponent = json.getString("exponent");
        // 密码加密
        String encryptPassword = RSAUtil.encrypt(modulus, exponent, password);
        // System.out.println(encryptPassword);
        // 如果当前请求直接重定向进入教务系统首页，说明是已登录状态，无需再登录
        // 当评价执行过程出现异常导致webvpn未正确注销时，相同账号再次登录教务系统就会出现这个情况
        resultStr = httpClient.doGet(JW_URL + "/xtgl/login_slogin.html");
        // TODO: 2020-07-07 教务系统登录待完成
        System.out.println(httpClient.getCookie("wengine_vpn_ticketwebvpn_gdou_edu_cn"));
        if (!resultStr.contains("用户登录")){
            System.out.println(resultStr);
            System.out.println("用户已登录");
        }
        return null;
    }


    /**
     * 登录webvpn，获取cookie（HttpClient会自己维护cookie）
     * @throws Exception
     * @param httpClient
     */
    private void doWebvpnLogin(HttpClientUtil httpClient) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("auth_type", "local");
        params.put("username", WEBVPN_USERNAME);
        params.put("sms_code", "");
        params.put("password", WEBVPN_PASSWORD);
        params.put("needCaptcha", "false");
        String resultStr = httpClient.doPost(WEBVPN_URL + "/do-login?local_login=true", params);
        JSONObject json = JSON.parseObject(resultStr);
        if (!json.getBoolean("success")){
            if ("NEED_CONFIRM".equals(json.getString("error"))){
                // 当前需要确认登录
                log.info("踢掉已登录的webvpn客户端");
                doWebvpnConfirmLogin(httpClient);
            }else {
                log.error("内置webvpn异常：{}", resultStr);
                throw new CustomException(CommonResult.failed("内置webvpn异常，等待修复"));
            }
        }
    }

    /**
     * 执行确认登录，踢掉已登录的webvpn客户端
     * @param httpClient
     */
    private void doWebvpnConfirmLogin(HttpClientUtil httpClient) throws IOException {
        String resultStr = httpClient.doPost(WEBVPN_URL + "/do-confirm-login", new HashMap<>());
        JSONObject json = JSON.parseObject(resultStr);
        if (!json.getBoolean("success")){
            throw new CustomException(CommonResult.failed("内置webvpn异常，等待修复"));
        }
    }

    @Override
    public LoginResultV2 loginByOpenid(String openid) {
        return null;
    }

    @Override
    public ScoreQueryResponseDTO queryScore(ScoreQueryRquestDTO scoreQueryRquestDTO) {
        return null;
    }

    @Override
    public ExamQueryResponseDTO queryExam(ExamQueryRquestDTO examQueryRquestDTO) {
        return null;
    }

    @Override
    public List<String> getSocreYearOptionsList(String cookie) throws Exception {
        return null;
    }

    @Override
    public List<String> getExamYearOptionsList(String cookie) throws Exception {
        return null;
    }
}