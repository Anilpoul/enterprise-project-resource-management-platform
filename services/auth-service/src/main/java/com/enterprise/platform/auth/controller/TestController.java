package com.enterprise.platform.auth.controller;

import com.enterprise.platform.auth.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @PostMapping
    public String testValidation(
            @Valid @RequestBody RegisterRequest request
    ) {
        return "Validation Success";
    }

}