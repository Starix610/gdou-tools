package com.starix.gdou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Starix
 * @date 2019-09-11 23:11
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // 设置核心线程数
        executor.setMaxPoolSize(20);        // 设置最大线程数
        executor.setQueueCapacity(100)  ;      // 设置队列容量
        executor.setKeepAliveSeconds(60);    // 设置线程活跃时间（秒）
        executor.setThreadNamePrefix("async-task-");  // 设置默认线程名称
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // 设置拒绝策略
        executor.setWaitForTasksToCompleteOnShutdown(true); // 等待所有任务结束后再关闭线程池
        return executor;
    }
}
