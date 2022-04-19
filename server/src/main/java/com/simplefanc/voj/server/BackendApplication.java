package com.simplefanc.voj.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: chenfan
 * @Date: 2020/10/22 23:25
 * @Description:
 */
@EnableRetry
@EnableScheduling // 开启定时任务
@EnableDiscoveryClient // 开启注册发现
@SpringBootApplication
@EnableAsync(proxyTargetClass = true) //开启异步注解
@EnableTransactionManagement
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}