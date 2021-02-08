package com.starix.gdou.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Starix
 * @date 2020-07-20 23:05
 */
@Data
@Builder
public class WxMessage {

    /**
     * 1:text，可以直接显示在卡片里面
     * 2:html，点击以后查看，支持html
     * 3:md，markdown格式
     */
    public static final int CONTENT_TYPE_TEXT = 1;
    public static final int CONTENT_TYPE_HTML = 2;
    public static final int CONTENT_TYPE_MD = 3;


    private String appToken;

    private List<String> uids;

    private List<Long> topicIds;

    private Integer contentType;

    private String content;

    private String summary;
    /**
     * 消息附带的url，仅针对text消息类型有效
     */
    private String url;

}
