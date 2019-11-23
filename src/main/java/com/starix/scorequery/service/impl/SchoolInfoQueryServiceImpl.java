package com.starix.scorequery.service.impl;

import com.starix.scorequery.entity.Student;
import com.starix.scorequery.exception.CustomException;
import com.starix.scorequery.repository.StudentRepository;
import com.starix.scorequery.response.CommonResult;
import com.starix.scorequery.service.SchoolInfoQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tobu
 * @date 2019-11-23 16:43
 */
@Service
public class SchoolInfoQueryServiceImpl implements SchoolInfoQueryService {

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
        student.setXh(xh);
        student.setPassword(password);
        studentRepository.save(student);
    }
}
