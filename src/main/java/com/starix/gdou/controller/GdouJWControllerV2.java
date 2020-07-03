package com.starix.gdou.controller;

import com.starix.gdou.dto.LoginResult;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.response.ResultCode;
import com.starix.gdou.service.GdouJWService;
import com.starix.gdou.service.GdouJWServiceV2;
import com.starix.gdou.service.UserBindService;
import com.starix.gdou.vo.ExamVO;
import com.starix.gdou.vo.QuerySocreRequestVO;
import com.starix.gdou.vo.ScoreVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * v1版本controller
 * @author shiwenjie
 * @created 2020/7/1 2:45 下午
 */
@RestController
@CrossOrigin
@Slf4j
public class GdouJWControllerV2 {

    @Autowired
    private GdouJWServiceV2 gdouJWService;
    @Autowired
    private UserBindService userBindService;

    @PostMapping("/login")
    public CommonResult doLogin(String username, String password, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        String cookie = gdouJWService.login(username, password);
        httpSession.setAttribute("jw_cookie", cookie);
        return CommonResult.success();
    }


    @PostMapping("/autoLogin")
    public CommonResult doAuoLogin(String openid, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(openid)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        String cookie = gdouJWService.loginByOpenid(openid);
        httpSession.setAttribute("jw_cookie", cookie);
        return CommonResult.success();
    }



    @PostMapping("/bind")
    public CommonResult doBind(String openid, String xh, String password) throws Exception {
        if (StringUtils.isEmpty(openid) || StringUtils.isEmpty(xh) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        log.info("[{}]正在绑定学号",xh);
        // 绑定前先验证账号密码是否正确
        gdouJWService.login(xh, password);
        userBindService.bind(openid, xh, password);
        return CommonResult.success();
    }

    @GetMapping("/queryScore")
    public CommonResult doQueryScore(String year, String semester, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        String jwCookie = httpSession.getAttribute("jw_cookie").toString();
        if (StringUtils.isEmpty(jwCookie)){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        log.info("[{}]正在查询成绩",loginResult.getXh());
        ScoreQueryRquestDTO.builder().cookie();
        List<ScoreVO> scoreList = gdouJWService.queryScore(loginResult, year, semester);

        return CommonResult.success(scoreList);
    }


    @GetMapping("/queryExam")
    public CommonResult doQueryExam(String year, String semester, HttpSession httpSession) throws Exception {

        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }

        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");

        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }

        log.info("[{}]正在查询考试", loginResult.getXh());
        List<ExamVO> scoreList = gdouJWService.getExam(loginResult, year, semester);

        return CommonResult.success(scoreList);
    }




    //获得查成绩页面年份下拉列表数据
    @GetMapping("/getScoreYearOptionsList")
    public CommonResult doGetScoreYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = gdouJWService.getSocreYearOptionsList(loginResult);
        return CommonResult.success(yearOptionsList);
    }


    //获得查考试页面年份下拉列表数据
    @GetMapping("/getExamYearOptionsList")
    public CommonResult doGetExamYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = gdouJWService.getExamYearOptionsList(loginResult);
        return CommonResult.success(yearOptionsList);
    }


    //自动评教
    @PostMapping("/autoEvaluate")
    public CommonResult doAutoEval(String xh, String password, String content, Integer mode) throws Exception {
        LoginResult loginResult = gdouJWService.login(xh, password);
        log.info("[{}]正在自动评教，参数-->[password]:{},[content]:{},[mode]:{}",xh,password,content,mode);
        gdouJWService.autoEvaluate(loginResult,content, mode);
        return CommonResult.success();
    }

}
