// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shiwenjie03
 * @created 2020/7/7 7:28 下午
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YearOptionListResponseDTO {

    /**
     * 学年列表数据value
     */
    private List<String> yearValueList;

    /**
     * 学年列表数据text
     */
    private List<String> yearTextList;

    /**
     * 学期列表数据value
     */
    private List<String> semesterValueList;

    /**
     * 学期列表数据text
     */
    private List<String> semesterTextList;

    /**
     * 当前选中学年的索引
     */
    private int selectedYear;

    /**
     * 当前选中学期的索引
     */
    private int selectedSemester;

}