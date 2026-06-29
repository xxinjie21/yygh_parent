package com.yygh.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
/**
 * 数据字典服务启动类
 *
 * @author XXJ
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = "com.yygh")
public class ServiceCmnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
