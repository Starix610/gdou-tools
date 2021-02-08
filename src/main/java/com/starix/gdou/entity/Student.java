package com.starix.gdou.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Starix
 * @date 2019-11-23 20:19
 */
@Data
@Entity
@DynamicUpdate
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String openid;

    private String username;

    private String password;

    private Integer notifyStatus;

    private Date createTime;

    private Date updateTime;

    private String email;


}
