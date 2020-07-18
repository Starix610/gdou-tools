package com.starix.gdou.service.impl;

import com.starix.gdou.entity.Student;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.repository.StudentRepository;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.UserBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Starix
 * @date 2019-11-23 16:43
 */
@Service
public class UserBindServiceImpl implements UserBindService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public boolean isBinding(String openid) {

        Student student = studentRepository.findByOpenid(openid);

        return student != null;
    }

    @Override
    public void bind(String openid, String xh, String password) {
        if (isBinding(openid)){
            throw new CustomException(CommonResult.failed("你已经绑定过学号啦"));
        }
        Student student = new Student();
        student.setOpenid(openid);
        student.setUsername(xh);
        student.setPassword(password);
        student.setNotifyStatus(0);
        student.setCreateTime(new Date());
        studentRepository.save(student);
    }

    @Override
    public String queryUsernameByOpenid(String openid) {
        Student student = studentRepository.findByOpenid(openid);
        if (student != null){
            return student.getUsername();
        }
        return null;
    }
}
