package com.starix.gdou.controller;

import com.starix.gdou.response.CommonResult;
import com.starix.gdou.response.ResultCode;
import com.starix.gdou.service.GdouJWScoreNotifyService;
import com.starix.gdou.utils.WxMessagePushUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static com.starix.gdou.common.Constant.WX_PUSH_TOKEN_MAIN_LOG;

/**
 * @author Starix
 * @date 2020-07-17 21:29
 */
@RestController
@RequestMapping("/jw/notify")
@CrossOrigin
@Slf4j
public class GdouJWScoreNotifyController {

    @Autowired
    private GdouJWScoreNotifyService gdouJWScoreNotifyService;

    @PostMapping("/enable")
    public CommonResult enableNotify(String openid, String email, int operation) throws Exception {
        log.info("开启/关闭成绩更新通知, openid: {}, email: {}, operation: {}", openid, email, operation);
        WxMessagePushUtil.push(WX_PUSH_TOKEN_MAIN_LOG,
                String.format("开启成绩更新通知, openid: %s, email: %s", openid, email));
        if (StringUtils.isEmpty(email)){
            return CommonResult.failed(ResultCode.VALIDATE_FAILED);
        }
        if (StringUtils.isEmpty(openid)){
            WxMessagePushUtil.push(WX_PUSH_TOKEN_MAIN_LOG, "开启成绩通知页面未获取到关注用户信息");
            return CommonResult.failed("未获取到关注用户的信息，请从公众号内进入该页面再进行操作！");
        }
        if (operation == 1){
            gdouJWScoreNotifyService.enableNotify(openid, email);
        } else {
            gdouJWScoreNotifyService.disableNotify(openid);
        }
        return CommonResult.success();
    }

    @GetMapping("/status")
    public CommonResult getStatus(String openid) throws Exception {
        log.info("获取成绩更新通知状态, openid: {}", openid);
        if (StringUtils.isEmpty(openid)){
            WxMessagePushUtil.push(WX_PUSH_TOKEN_MAIN_LOG, "开启成绩通知页面未获取到关注用户信息");
            return CommonResult.failed("未获取到关注用户的信息，请从公众号内进入该页面再进行操作！");
        }
        return CommonResult.success(gdouJWScoreNotifyService.queryNotifyStatus(openid));
    }
}
