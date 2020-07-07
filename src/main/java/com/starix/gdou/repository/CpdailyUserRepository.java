package com.starix.gdou.repository;

import com.starix.gdou.entity.CpdailyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Starix
 * @date 2020-04-06 12:14
 */
public interface CpdailyUserRepository extends JpaRepository<CpdailyUser, Integer> {

    CpdailyUser findByUsername(String username);

    void deleteByUsername(String username);

}
