package com.nxboot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NxBoot 启动入口
 */
@SpringBootApplication(scanBasePackages = "com.nxboot")
@EnableScheduling
public class NxBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(NxBootApplication.class, args);
    }
}
