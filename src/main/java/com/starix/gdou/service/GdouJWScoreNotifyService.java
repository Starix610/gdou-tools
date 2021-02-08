package com.starix.gdou.service;

import java.util.Map;

/**
 * @author Starix
 * @date 2020-07-17 21:59
 */
public interface GdouJWScoreNotifyService {

    void enableNotify(String openid, String email) throws Exception;

    void disableNotify(String openid);

    Map<String, Object> queryNotifyStatus(String openid);

    // 查询自定义的年份学期，不使用官网当前默认的年份学期
    String queryNotifyYearSemester();
}
