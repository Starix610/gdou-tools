package com.starix.gdou.vo;

import lombok.Data;

/**
 * @author Starix
 * @date 2019-11-20 20:54
 */
@Data
public class QuerySocreRequestVO {

    //学号
    private String xh;

    //密码
    private String password;

    //学年
    private String year;

    //学期
    private String semester;


}
