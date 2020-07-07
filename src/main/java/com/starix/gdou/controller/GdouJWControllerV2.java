package com.starix.gdou.controller;

import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ExamQueryRquestDTO;
import com.starix.gdou.dto.request.ScoreQueryRquestDTO;
import com.starix.gdou.dto.response.ExamQueryResponseDTO;
import com.starix.gdou.dto.response.ScoreQueryResponseDTO;
import com.starix.gdou.dto.response.YearOptionListResponseDTO;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.response.ResultCode;
import com.starix.gdou.service.GdouJWServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * v1版本controller
 * @author shiwenjie
 * @created 2020/7/1 2:45 下午
 */
@RestController
@RequestMapping("/jw")
@CrossOrigin
@Slf4j
public class GdouJWControllerV2 {

    @Autowired
    private GdouJWServiceV2 gdouJWService;

    @GetMapping("/test")
    public CommonResult test() throws Exception {
        return CommonResult.success();
    }

    @PostMapping("/login")
    public CommonResult doLogin(String username, String password, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        log.info("[{}]用户登录", username);
        LoginResultV2 loginResult = gdouJWService.login(username, password);
        httpSession.setAttribute("jw_cookie", loginResult);
        return CommonResult.success();
    }


    @PostMapping("/autoLogin")
    public CommonResult doAuoLogin(String openid, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(openid)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        LoginResultV2 loginResult = gdouJWService.loginByOpenid(openid);
        httpSession.setAttribute("jw_cookie", loginResult);
        return CommonResult.success();
    }


    @GetMapping("/queryScore")
    public CommonResult doQueryScore(String year, String semester, HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        log.info("[{}]查询成绩, 学年: {}, 学期: {}", loginResult.getUsername(), year, semester);
        ScoreQueryRquestDTO scoreQueryRquestDTO = ScoreQueryRquestDTO.builder()
                .cookie(loginResult.getCookie())
                .year(year)
                .semester(semester)
                .build();
        List<ScoreQueryResponseDTO> resultList = gdouJWService.queryScore(scoreQueryRquestDTO);
        return CommonResult.success(resultList);
    }


    @GetMapping("/queryExam")
    public CommonResult doQueryExam(String year, String semester, HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        log.info("[{}]查询考试, 学年: {}, 学期: {}", loginResult.getUsername(), year, semester);
        ExamQueryRquestDTO examQueryRquestDTO = ExamQueryRquestDTO.builder()
                .cookie(loginResult.getCookie())
                .year(year)
                .semester(semester)
                .build();
        ExamQueryResponseDTO examQueryResponseDTO = gdouJWService.queryExam(examQueryRquestDTO);
        return CommonResult.success(examQueryResponseDTO);
    }




    //获得查成绩页面年份下拉列表数据
    @GetMapping("/getScoreYearOptionsList")
    public CommonResult doGetScoreYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
       if (loginResult == null){
           return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
       }
        YearOptionListResponseDTO yearOptionListResponseDTO = gdouJWService.getSocreYearOptionList(loginResult.getCookie());
        return CommonResult.success(yearOptionListResponseDTO);
    }


    //获得查考试页面年份下拉列表数据
    @GetMapping("/getExamYearOptionsList")
    public CommonResult doGetExamYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = gdouJWService.getExamYearOptionList(loginResult.getCookie());
        return CommonResult.success(yearOptionsList);
    }

}
