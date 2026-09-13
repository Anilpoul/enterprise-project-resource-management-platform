package com.enterprise.platform.user.kafka.consumer;

import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.events.AuthEventType;
import com.enterprise.platform.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEventConsumerTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private ObjectMapper objectMapper;

    private AuthEventConsumer authEventConsumer;

    @BeforeEach
    void setUp() {
        authEventConsumer = new AuthEventConsumer(objectMapper, userProfileService);
    }

    @Test
    @DisplayName("Should invoke createUserProfile when USER_REGISTERED event is received")
    void testConsumeUserRegistered() {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_REGISTERED)
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .timestamp(LocalDateTime.now())
                .details("New user")
                .build();

        authEventConsumer.consume(event);

        verify(userProfileService, times(1)).createUserProfile(event);
    }

    @Test
    @DisplayName("Should safely ignore other event types without throwing exception")
    void testConsumeOtherEvents() {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_LOGGED_IN)
                .email("john@example.com")
                .timestamp(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> authEventConsumer.consume(event));

        verify(userProfileService, never()).createUserProfile(any());
    }
}
