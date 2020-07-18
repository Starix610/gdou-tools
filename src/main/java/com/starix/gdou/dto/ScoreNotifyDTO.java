package com.starix.gdou.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Starix
 * @date 2020-07-18 15:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreNotifyDTO {

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
     * 课程性质
     */
    private String property;

    /**
     * 是否是刚更新的成绩
     */
    private boolean isNew;

}
