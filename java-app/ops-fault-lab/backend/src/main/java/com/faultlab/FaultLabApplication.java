package com.faultlab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端主入口负责启动 API、故障场景与运维监控能力。
 */
@SpringBootApplication
@MapperScan("com.faultlab.mapper")
public class FaultLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaultLabApplication.class, args);
    }
}
