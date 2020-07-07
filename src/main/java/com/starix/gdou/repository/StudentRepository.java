package com.starix.gdou.repository;

import com.starix.gdou.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Starix
 * @date 2019-11-23 20:16
 */
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Student findByOpenid(String openid);

}
