package com.ops.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpsMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsMonitorApplication.class, args);
    }
}
