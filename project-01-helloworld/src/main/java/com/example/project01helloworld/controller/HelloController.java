package com.example.project01helloworld.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello from Gradle + springboot";
    }
    @GetMapping("/bye")
    public String bye(){
        return "Bye from Gradle + springboot";
    }
}
