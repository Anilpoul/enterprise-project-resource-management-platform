package com.enterprise.platform.auth.kafka.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AuthEvent {

    private UUID eventId;

    private AuthEventType eventType;

    private String email;

    private LocalDateTime timestamp;

    private String details;

}