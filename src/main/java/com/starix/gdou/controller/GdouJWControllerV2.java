package com.starix.gdou.controller;

import com.starix.gdou.dto.LoginResult;
import com.starix.gdou.dto.LoginResultV2;
import com.starix.gdou.dto.request.ExamQueryRquestDTO;
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

    @GetMapping("/test")
    public CommonResult test() throws Exception {
        gdouJWService.login("xxx", "xxx");
        return CommonResult.success();
    }

    @PostMapping("/login")
    public CommonResult doLogin(String username, String password, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
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



    @PostMapping("/bind")
    public CommonResult doBind(String openid, String username, String password) throws Exception {
        if (StringUtils.isEmpty(openid) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        log.info("[{}]正在绑定学号",username);
        // 绑定前先验证账号密码是否正确
        gdouJWService.login(username, password);
        userBindService.bind(openid, username, password);
        return CommonResult.success();
    }

    @GetMapping("/queryScore")
    public CommonResult doQueryScore(String year, String semester, HttpSession httpSession) throws Exception {
        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        log.info("[{}]正在查询成绩",loginResult.getUsername());
        ScoreQueryRquestDTO scoreQueryRquestDTO = ScoreQueryRquestDTO.builder()
                .cookie(loginResult.getCookie())
                .year(year)
                .semester(semester)
                .build();
        gdouJWService.queryScore(scoreQueryRquestDTO);
        return CommonResult.success();
    }


    @GetMapping("/queryExam")
    public CommonResult doQueryExam(String year, String semester, HttpSession httpSession) throws Exception {

        if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        ExamQueryRquestDTO examQueryRquestDTO = ExamQueryRquestDTO.builder()
                .cookie(loginResult.getCookie())
                .year(year)
                .semester(semester)
                .build();
        log.info("[{}]正在查询考试", loginResult.getUsername());
        gdouJWService.queryExam(examQueryRquestDTO);
        return CommonResult.success();
    }




    //获得查成绩页面年份下拉列表数据
    @GetMapping("/getScoreYearOptionsList")
    public CommonResult doGetScoreYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = gdouJWService.getSocreYearOptionsList(loginResult.getUsername());
        return CommonResult.success(yearOptionsList);
    }


    //获得查考试页面年份下拉列表数据
    @GetMapping("/getExamYearOptionsList")
    public CommonResult doGetExamYearOptionsList(HttpSession httpSession) throws Exception {
        LoginResultV2 loginResult = (LoginResultV2) httpSession.getAttribute("jw_cookie");
        if (loginResult == null){
            return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
        }
        List<String> yearOptionsList = gdouJWService.getExamYearOptionsList(loginResult.getCookie());
        return CommonResult.success(yearOptionsList);
    }

}
