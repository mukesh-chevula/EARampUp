package com.example.cricketstreamer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.cricketstreamer", "com.example.config", "com.example.grpc"})
public class CricketStreamerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CricketStreamerApplication.class, args);
    }

}
