package com.enterprise.platform.auth.controller;

import com.enterprise.platform.auth.dto.request.LoginRequest;
import com.enterprise.platform.auth.dto.request.RefreshTokenRequest;
import com.enterprise.platform.auth.dto.request.RegisterRequest;
import com.enterprise.platform.auth.dto.response.ApiResponse;
import com.enterprise.platform.auth.dto.response.AuthResponse;
import com.enterprise.platform.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authenticationService.register(request))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authenticationService.login(request))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(authenticationService.refreshToken(
                        request.getRefreshToken()
                ))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        authenticationService.logout(
                request.getRefreshToken(),
                request.getAccessToken()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .timestamp(LocalDateTime.now())
                .build();
    }

}