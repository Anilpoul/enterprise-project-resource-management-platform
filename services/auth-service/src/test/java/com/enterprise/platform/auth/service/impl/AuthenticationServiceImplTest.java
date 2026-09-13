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
import com.enterprise.platform.auth.kafka.producer.AuthEventProducer;
import com.enterprise.platform.auth.repository.RoleRepository;
import com.enterprise.platform.auth.repository.UserRepository;
import com.enterprise.platform.auth.security.jwt.JwtClaimsFactory;
import com.enterprise.platform.auth.security.jwt.JwtService;
import com.enterprise.platform.auth.service.RefreshTokenService;
import com.enterprise.platform.auth.service.token.TokenBlacklistService;
import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.events.AuthEventType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private AuthEventProducer authEventProducer;

    @Mock
    private JwtClaimsFactory jwtClaimsFactory;

    @Captor
    private ArgumentCaptor<AuthEvent> authEventCaptor;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User sampleUser;
    private Role defaultRole;
    private RefreshToken sampleRefreshToken;

    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setId(UUID.randomUUID());
        defaultRole.setName(RoleType.ROLE_TEAM_MEMBER);

        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setFirstName("Alice");
        sampleUser.setLastName("Smith");
        sampleUser.setEmail("alice@example.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setStatus(UserStatus.ACTIVE);
        sampleUser.setRoles(Set.of(defaultRole));

        sampleRefreshToken = new RefreshToken();
        sampleRefreshToken.setId(UUID.randomUUID());
        sampleRefreshToken.setToken("refresh-token-uuid-1234");
        sampleRefreshToken.setUser(sampleUser);
        sampleRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        sampleRefreshToken.setRevoked(false);
    }

    @Test
    @DisplayName("Should successfully register a new user, publish USER_REGISTERED event, and return AuthResponse")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_TEAM_MEMBER)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtClaimsFactory.buildClaims(any(User.class))).thenReturn(Jwts.claims().build());
        when(jwtService.generateToken(any(), any(UserDetails.class))).thenReturn("access-token-xyz");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(sampleRefreshToken);

        AuthResponse response = authenticationService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-xyz");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid-1234");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(authEventProducer).publish(authEventCaptor.capture());
        AuthEvent event = authEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(AuthEventType.USER_REGISTERED);
        assertThat(event.getUserId()).isEqualTo(sampleUser.getId());
        assertThat(event.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should throw BadRequestException when registering with duplicate email")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");

        verify(authEventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Should successfully login user, populate userId in USER_LOGGED_IN event, and return tokens")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(jwtClaimsFactory.buildClaims(sampleUser)).thenReturn(Jwts.claims().build());
        when(jwtService.generateToken(any(), any(UserDetails.class))).thenReturn("access-token-xyz");
        when(refreshTokenService.createRefreshToken(sampleUser)).thenReturn(sampleRefreshToken);

        AuthResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-xyz");

        verify(authEventProducer).publish(authEventCaptor.capture());
        AuthEvent event = authEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(AuthEventType.USER_LOGGED_IN);
        assertThat(event.getUserId()).isEqualTo(sampleUser.getId());
        assertThat(event.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should throw BadRequestException on failed login and publish LOGIN_FAILED event")
    void testLoginFailed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid credentials");

        verify(authEventProducer).publish(authEventCaptor.capture());
        AuthEvent event = authEventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(AuthEventType.LOGIN_FAILED);
        assertThat(event.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should successfully rotate refresh token and issue new access token")
    void testRefreshToken() {
        RefreshToken rotatedToken = new RefreshToken();
        rotatedToken.setToken("new-refresh-token-456");
        rotatedToken.setUser(sampleUser);

        when(refreshTokenService.verifyRefreshToken("old-token")).thenReturn(sampleRefreshToken);
        when(refreshTokenService.rotateRefreshToken(sampleRefreshToken)).thenReturn(rotatedToken);
        when(jwtClaimsFactory.buildClaims(sampleUser)).thenReturn(Jwts.claims().build());
        when(jwtService.generateToken(any(), any(UserDetails.class))).thenReturn("new-access-token");

        AuthResponse response = authenticationService.refreshToken("old-token");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token-456");
    }

    @Test
    @DisplayName("Should revoke token and blacklist access token on logout")
    void testLogout() {
        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        authenticationService.logout("refresh-token", "access-token");

        verify(refreshTokenService).revokeToken("refresh-token");
        verify(tokenBlacklistService).blacklistToken("access-token");
        verify(authEventProducer).publish(authEventCaptor.capture());
        assertThat(authEventCaptor.getValue().getEventType()).isEqualTo(AuthEventType.USER_LOGGED_OUT);
        assertThat(authEventCaptor.getValue().getUserId()).isEqualTo(sampleUser.getId());
    }
}
