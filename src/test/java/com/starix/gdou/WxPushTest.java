package com.starix.gdou;

import com.alibaba.fastjson.JSONObject;
import com.starix.gdou.common.Constant;
import com.starix.gdou.dto.WxMessage;
import com.starix.gdou.utils.HttpClientUtil;
import com.starix.gdou.utils.WxMessagePushUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Starix
 * @date 2020-07-19 23:52
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class WxPushTest {

    @Test
    public void testPushWxMessage() throws IOException {
        HttpClientUtil httpClient = new HttpClientUtil();
        WxMessage wxMessage = WxMessage.builder()
                .appToken("AT_ZDSHlFh0W3wGpBl7wGn1owM2somLTTJO")
                .uids(Arrays.asList("UID_RrJlsX3ns3sncf2ZI58UyhliOq9f"))
                .contentType(WxMessage.CONTENT_TYPE_TEXT)
                .content("测试发送")
                .build();
        httpClient.doPost("http://wxpusher.zjiecode.com/api/send/message", JSONObject.toJSONString(wxMessage));
    }

    @Test
    public void testPushWxMessageUtil() {
        WxMessagePushUtil.push(Constant.WX_PUSH_TOKEN_MAIN_LOG, "测试来了");
    }

}
