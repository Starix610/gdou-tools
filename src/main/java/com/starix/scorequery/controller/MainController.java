package com.starix.scorequery.controller;

import com.starix.scorequery.pojo.LoginResult;
import com.starix.scorequery.response.CommonResult;
import com.starix.scorequery.response.ResultCode;
import com.starix.scorequery.service.SpiderService;
import com.starix.scorequery.vo.ExamVO;
import com.starix.scorequery.vo.ScoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Tobu
 * @date 2019-11-18 18:56
 */
@RestController
@CrossOrigin
public class MainController {

    @Autowired
    private SpiderService spiderService;

    @PostMapping("/login")
    public CommonResult doLogin(String xh, String password, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(xh) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        LoginResult loginResult = spiderService.login(xh, password);

        httpSession.setAttribute("studentLoginInfo", loginResult);

        return CommonResult.success();
    }


    @GetMapping("/queryScore")
    public CommonResult doQueryScore(String year, String semester, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }

        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");

        if (loginResult == null){
            return CommonResult.fail(ResultCode.UNAUTHORIZED.getCode(),"你还没有登录或者登录信息已经过期");
        }

        List<ScoreVO> scoreList = spiderService.getScore(loginResult, year, semester);

        return CommonResult.success(scoreList);
    }


    @GetMapping("/queryExam")
    public CommonResult doQueryExam(String year, String semester, HttpSession httpSession) throws Exception {

        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }

        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");

        if (loginResult == null){
            return CommonResult.fail(ResultCode.UNAUTHORIZED.getCode(),"你还没有登录或者登录信息已经过期");
        }

        List<ExamVO> scoreList = spiderService.getExam(loginResult, year, semester);

        return CommonResult.success(scoreList);
    }




    //获得查成绩页面年份下拉列表数据
    @GetMapping("/getScoreYearOptionsList")
    public CommonResult doGetScoreYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
        if (loginResult == null){
            return CommonResult.fail(ResultCode.UNAUTHORIZED.getCode(),"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = spiderService.getSocreYearOptionsList(loginResult);
        return CommonResult.success(yearOptionsList);
    }


    //获得查考试页面年份下拉列表数据
    @GetMapping("/getExamYearOptionsList")
    public CommonResult doGetExamYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
        if (loginResult == null){
            return CommonResult.fail(ResultCode.UNAUTHORIZED.getCode(),"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = spiderService.getExamYearOptionsList(loginResult);
        return CommonResult.success(yearOptionsList);
    }


}
