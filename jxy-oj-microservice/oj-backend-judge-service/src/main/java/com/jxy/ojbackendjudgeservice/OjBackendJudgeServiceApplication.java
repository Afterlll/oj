package com.jxy.ojbackendjudgeservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jxy.ojbackendjudgeservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.jxy")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.jxy.ojbackendserviceclient"})
public class OjBackendJudgeServiceApplication {

    public static void main(String[] args) {
        // 在启动判题服务之前开启消息队列初始化
        // 初始化消息队列，先注释掉，改用 Bean 的方式初始化消息队列（InitRabbitMqBean.java）
//        InitRabbitMq.doInit();
        SpringApplication.run(OjBackendJudgeServiceApplication.class, args);
    }

}
