// Copyright (C) 2020 Meituan
// All rights reserved
package com.starix.gdou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * v2版本：
 * 分装登录后的cookie以及用户名信息，用于维持教务系统登录态，无需每次请求都要重新登录
 *
 * @author shiwenjie
 * @created 2020/7/1 4:02 下午
 **/
@Data
@AllArgsConstructor
public class LoginResultV2 {

    private String cookie;

    private String username;

}