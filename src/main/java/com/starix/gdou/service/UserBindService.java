package com.starix.gdou.service;

/**
 * @author Starix
 * @date 2019-11-23 16:43
 */
public interface UserBindService {

    /**
     * 根据openid判断当前用户是否已经绑定过学号
     * @param openid openid
     * @return 已绑定返回true，否则返回false
     */
    boolean isBinding(String openid);

    void bind(String openid, String xh, String password);
}
