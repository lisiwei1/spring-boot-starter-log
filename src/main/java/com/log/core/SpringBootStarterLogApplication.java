package com.log.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author lsw
 * @Date 2023/5/18 14:51
 */
@EnableAsync
@SpringBootApplication
public class SpringBootStarterLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterLogApplication.class, args);
    }

}
