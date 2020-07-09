// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.controller;

import com.starix.gdou.response.CommonResult;
import com.starix.gdou.response.ResultCode;
import com.starix.gdou.service.GdouJWServiceV2;
import com.starix.gdou.service.UserBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shiwenjie03
 * @created 2020/7/7 5:08 下午
 **/
@RestController
@RequestMapping("/jw")
@CrossOrigin
@Slf4j
public class GdouJWUserBindController {

    @Autowired
    private UserBindService userBindService;
    @Autowired
    private GdouJWServiceV2 gdouJWService;

    @PostMapping("/bind")
    public CommonResult doBind(String openid, String username, String password) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        if (StringUtils.isEmpty(openid)){
            return CommonResult.failed("未获取到关注用户的信息，请从公众号内进入该页面再进行绑定！");
        }
        log.info("[{}]绑定学号", username);
        // 绑定前先验证账号密码是否正确
        gdouJWService.login(username, password);
        userBindService.bind(openid, username, password);
        return CommonResult.success();
    }


    // TODO: 2020/7/7 解绑账号待实现，这个不重要
//    @PostMapping("/unbind")
//    public CommonResult doUnbind(String openid, String username, String password) throws Exception {
//        if (!checkBindParams(openid, username, password)){
//            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//        }
//        log.info("[{}]绑定学号", username);
//        // 解除绑定前先验证账号密码是否正确
//        gdouJWService.login(username, password);
//        userBindService.unbind(openid, username, password);
//        return CommonResult.success();
//    }

    private boolean checkBindParams(String openid, String username, String password){
        return !StringUtils.isEmpty(openid) && !StringUtils.isEmpty(username) && !StringUtils.isEmpty(password);
    }

}