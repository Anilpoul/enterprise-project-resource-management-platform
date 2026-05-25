package com.enterprise.platform.auth.service;

import com.enterprise.platform.auth.dto.request.LoginRequest;
import com.enterprise.platform.auth.dto.request.RegisterRequest;
import com.enterprise.platform.auth.dto.response.AuthResponse;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken, String accessToken);

}