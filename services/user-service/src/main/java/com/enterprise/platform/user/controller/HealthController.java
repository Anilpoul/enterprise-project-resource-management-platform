package com.enterprise.platform.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "USER SERVICE UP";
    }
}