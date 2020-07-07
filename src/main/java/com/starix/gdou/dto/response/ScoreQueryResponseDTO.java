// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * @author shiwenjie
 * @created 2020/7/1 2:45 下午
 **/
@Data
@Builder
public class ScoreQueryResponseDTO {

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 学分
     */
    private String credit;

    /**
     * 成绩
     */
    private String score;

    /**
     * 绩点
     */
    private String gpa;

    /**
     * 课程性质
     */
    private String property;

    /**
     * 课程类别
     */
    private String category;

    /**
     * 课程归属
     */
    private String belongTo;

}