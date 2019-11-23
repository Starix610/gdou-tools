package com.starix.scorequery.repository;

import com.starix.scorequery.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Tobu
 * @date 2019-11-23 20:16
 */
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Student findByOpenid(String openid);

}
