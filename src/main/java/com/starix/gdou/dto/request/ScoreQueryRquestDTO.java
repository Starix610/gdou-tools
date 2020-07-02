package com.starix.gdou.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author shiwenjie
 * @created 2020/7/1 2:38 下午
 **/
@Data
@Builder
public class ScoreQueryRquestDTO {

    private String cookie;

    private String year;

    private String semester;

}