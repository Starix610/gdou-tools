package com.starix.gdou.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * (CpdailyUser)表实体类
 *
 * @author Starix
 * @since 2020-04-06 12:11:41
 */
@SuppressWarnings("serial")
@Data
@Entity
public class CpdailyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //用户名（学号）
    @NotBlank(message = "用户名不能为空")
    private String username;
    //密码
    @NotBlank(message = "密码不能为空")
    private String password;
    //经度
    @NotBlank(message = "经度不能为空")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{6}$", message = "经度数据格式有误")
    private String longitude;
    //纬度
    @NotBlank(message = "纬度不能为空")
    @Pattern(regexp = "^[0-9]+\\.[0-9]{6}$", message = "纬度数据格式有误")
    private String latitude;
    //签到内容
    @NotBlank(message = "签到内容不能为空")
    private String abnormalreason;
    //地理位置
    @NotBlank(message = "地理位置不能为空")
    private String position;
    //邮箱
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式有误")
    private String email;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;

}