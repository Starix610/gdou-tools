package com.starix.gdou.utils;

import com.alibaba.fastjson.JSONObject;
import com.starix.gdou.common.Constant;
import com.starix.gdou.dto.WxMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static com.starix.gdou.common.Constant.*;

/**
 * @author Starix
 * @date 2020-07-21 0:03
 */
@Slf4j
public class WxMessagePushUtil {

    private static final String PUSH_URL = "http://wxpusher.zjiecode.com/api/send/message";

    public static void push(String token, String message){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date()) + "\n";
        String split = "-------------------------------\n";
        String content = "";
        switch (token){
            case WX_PUSH_TOKEN_MAIN_LOG:
                content = "[业务日志]\n";
                break;
            case WX_PUSH_TOKEN_EXCEPTION:
                content = "[异常日志]\n";
                break;
            case WX_PUSH_TOKEN_MP_MSG:
                content = "[公众号消息]\n";
                break;
            case WX_PUSH_TOKEN_SUBSCRIBE:
                // content = "[用户关注/取关]\n";
                break;

        }
        HttpClientUtil httpClient = new HttpClientUtil();
        WxMessage wxMessage = WxMessage.builder()
                .appToken(token)
                .uids(Arrays.asList(Constant.WX_PUSH_UID))
                .contentType(WxMessage.CONTENT_TYPE_TEXT)
                .content(content + date + split + message)
                .build();
        try {
            httpClient.doPost(PUSH_URL, JSONObject.toJSONString(wxMessage));
        } catch (IOException e) {
            log.error("推送微信消息失败", e);
        }
    }

}
