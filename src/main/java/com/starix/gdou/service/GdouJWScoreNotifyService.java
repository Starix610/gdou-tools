package com.starix.gdou.service;

/**
 * @author Starix
 * @date 2020-07-17 21:59
 */
public interface GdouJWScoreNotifyService {

    void enableNotify(String openid, String email) throws Exception;

    void disableNotify(String openid);
}
