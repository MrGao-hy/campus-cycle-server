package com.campus.cycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园循环 - 校园二手交易平台
 * 启动入口
 */
@SpringBootApplication
@MapperScan("com.campus.cycle.mapper")
@EnableScheduling
public class CampusCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCycleApplication.class, args);
    }
}
