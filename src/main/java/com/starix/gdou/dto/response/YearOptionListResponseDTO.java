// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.dto.response;

import lombok.AllArgsConstructor;
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
public class YearOptionListResponseDTO {

    /**
     * 学年列表数据
     */
    private List<String> yearList;

    /**
     * 当前选中学年的索引
     */
    private int selectedYear;


    /**
     * 当前选中学年的索引
     */
    private int selectedSemester;

}