// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

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


    /**
     * 重写eauals，用于成绩更新的List判断
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        ScoreQueryResponseDTO other = (ScoreQueryResponseDTO) obj;
        if (StringUtils.isEmpty(other.getCourseName())){
            return false;
        }
        if (StringUtils.isEmpty(courseName)){
            if (!StringUtils.isEmpty(other.getCourseName())){
                return false;
            }
        }else if (!courseName.equals(other.getCourseName())){
            return false;
        }
        return true;
    }

}