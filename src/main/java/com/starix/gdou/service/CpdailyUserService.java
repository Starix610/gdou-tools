package com.starix.gdou.service;

import com.starix.gdou.entity.CpdailyUser;

/**
 * @author Starix
 * @date 2020-04-06 13:27
 */
public interface CpdailyUserService {

    void saveUser(CpdailyUser user) throws Exception;

    void deleteUser(String username, String password) throws Exception;

    void updateUser(CpdailyUser user) throws Exception;
}
