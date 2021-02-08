package com.starix.gdou.task;

import com.starix.gdou.entity.CpdailyUser;
import com.starix.gdou.repository.CpdailyUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * TODO 停止使用
 * @author Starix
 * @date 2020-04-06 1:00
 */
@Slf4j
// @Component
public class CpdailyAutoSignInSchedule {

    @Autowired
    private CpdailySignInAsyncTask signInAsyncTask;
    @Autowired
    private CpdailyUserRepository userRepository;

    //定时为已记录的账号进行签到，每天中午12点执行一次
    // @Scheduled(fixedRate = 1000*60*60*24)
    // @Scheduled(cron = "0 0 12 * * ?")
    public void signIntask() throws Exception {
        log.info("==============开始执行签到==============");
        List<CpdailyUser> userList = userRepository.findAll();
        for (CpdailyUser cpdailyUser : userList) {
            //异步任务签到
            signInAsyncTask.doSignIn(cpdailyUser);
        }
    }

}
