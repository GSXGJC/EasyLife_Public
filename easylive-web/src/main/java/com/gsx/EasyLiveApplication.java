package com.gsx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.gsx.*")
public class EasyLiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyLiveApplication.class, args);
    }
}