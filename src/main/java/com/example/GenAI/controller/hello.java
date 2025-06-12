package com.example.GenAI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class hello {
    @GetMapping("/")
    public String home() {
        return "Hello, Spring Boot!";
    }

}
