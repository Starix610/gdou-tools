package com.starix.gdou.dto.request;

import lombok.Builder;
import lombok.Data;
import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * @author shiwenjie
 * @created 2020/7/1 2:38 下午
 **/
@Data
@Builder
public class ExamQueryRquestDTO {

    private List<Cookie> cookies;

    private String year;

    private String semester;

}