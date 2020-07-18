package com.starix.gdou.controller;

import com.starix.gdou.response.CommonResult;
import com.starix.gdou.response.ResultCode;
import com.starix.gdou.service.GdouJWScoreNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Starix
 * @date 2020-07-17 21:29
 */
@RestController
@RequestMapping("/jw")
@CrossOrigin
@Slf4j
public class GdouJWScoreNotifyController {

    @Autowired
    private GdouJWScoreNotifyService gdouJWScoreNotifyService;

    @PostMapping("/enableNotify")
    public CommonResult enableNotify(String openid, String email) throws Exception {
        log.info("开启成绩更新通知, openid: {}, email: {}", openid, email);
        if (StringUtils.isEmpty(email)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        if (StringUtils.isEmpty(openid)){
            return CommonResult.failed("未获取到关注用户的信息，请从公众号内进入该页面再进行操作！");
        }
        gdouJWScoreNotifyService.enableNotify(openid, email);
        return CommonResult.success();
    }

    @PostMapping("/disableNotify")
    public CommonResult enableNotify(String openid){
        log.info("关闭成绩更新通知, openid: {}", openid);
        if (StringUtils.isEmpty(openid)){
            return CommonResult.failed("未获取到关注用户的信息，请从公众号内进入该页面再进行操作！");
        }
        gdouJWScoreNotifyService.disableNotify(openid);
        return CommonResult.success();
    }
}
