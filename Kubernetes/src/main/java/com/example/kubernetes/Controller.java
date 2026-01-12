package com.example.kubernetes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controller {
    
    @Value("${app.greeting}")
    private String greeting;
    
    @Value("${app.password}")
    private String password;
    
    @GetMapping("/ping")
    public String ping() { 
        return "pong"; 
    }
    
    @GetMapping("/config")
    public String config() {
        return "Greeting: " + greeting + ", Password configured: " + (password != null && !password.isEmpty());
    }
}
