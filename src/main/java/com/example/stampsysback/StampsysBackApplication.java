package com.example.stampsysback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.stampsysback.mapper")
public class StampsysBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(StampsysBackApplication.class, args);
    }

}
