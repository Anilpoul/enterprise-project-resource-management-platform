package com.enterprise.platform.auth.service.impl;

import com.enterprise.platform.auth.constant.enums.RoleType;
import com.enterprise.platform.auth.constant.enums.UserStatus;
import com.enterprise.platform.auth.dto.request.LoginRequest;
import com.enterprise.platform.auth.dto.request.RegisterRequest;
import com.enterprise.platform.auth.dto.response.AuthResponse;
import com.enterprise.platform.auth.entity.RefreshToken;
import com.enterprise.platform.auth.entity.Role;
import com.enterprise.platform.auth.entity.User;
import com.enterprise.platform.auth.exception.BadRequestException;
import com.enterprise.platform.auth.kafka.event.AuthEvent;
import com.enterprise.platform.auth.kafka.event.AuthEventType;
import com.enterprise.platform.auth.kafka.producer.AuthEventProducer;
import com.enterprise.platform.auth.repository.RoleRepository;
import com.enterprise.platform.auth.repository.UserRepository;
import com.enterprise.platform.auth.security.CustomUserDetails;
import com.enterprise.platform.auth.security.jwt.JwtService;
import com.enterprise.platform.auth.service.AuthenticationService;
import com.enterprise.platform.auth.service.RefreshTokenService;
import com.enterprise.platform.auth.service.token.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl
        implements AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    private final TokenBlacklistService tokenBlacklistService;

    private final AuthEventProducer authEventProducer;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new BadRequestException(
                    "Email already exists"
            );
        }

        Role defaultRole = roleRepository.findByName(
                        RoleType.ROLE_TEAM_MEMBER
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Default role not found"
                        )
                );

        User user = new User();

        user.setFirstName(request.getFirstName());

        user.setLastName(request.getLastName());

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setStatus(UserStatus.ACTIVE);

        user.setRoles(Set.of(defaultRole));

        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(
                new CustomUserDetails(savedUser)
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        authEventProducer.publish(
                AuthEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(
                                AuthEventType.USER_REGISTERED
                        )
                        .email(savedUser.getEmail())
                        .timestamp(LocalDateTime.now())
                        .details("New user registered")
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (Exception ex) {

            authEventProducer.publish(
                    AuthEvent.builder()
                            .eventId(UUID.randomUUID())
                            .eventType(
                                    AuthEventType.LOGIN_FAILED
                            )
                            .email(request.getEmail())
                            .timestamp(LocalDateTime.now())
                            .details("Invalid credentials")
                            .build()
            );

            throw new BadRequestException(
                    "Invalid credentials"
            );
        }

        User user = userRepository.findByEmail(
                        request.getEmail()
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid credentials"
                        )
                );

        String jwtToken = jwtService.generateToken(
                new CustomUserDetails(user)
        );


        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        authEventProducer.publish(
                AuthEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(
                                AuthEventType.USER_LOGGED_IN
                        )
                        .email(user.getEmail())
                        .timestamp(LocalDateTime.now())
                        .details("User logged in")
                        .build()
        );
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        refreshTokenValue
                );

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(
                new CustomUserDetails(user)
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }

    @Override
    public void logout(
            String refreshToken,
            String accessToken
    ) {
        authEventProducer.publish(
                AuthEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(
                                AuthEventType.USER_LOGGED_OUT
                        )
                        .email("USER")
                        .timestamp(LocalDateTime.now())
                        .details("User logged out")
                        .build()
        );
        refreshTokenService.revokeToken(refreshToken);

        tokenBlacklistService.blacklistToken(accessToken);
    }

}