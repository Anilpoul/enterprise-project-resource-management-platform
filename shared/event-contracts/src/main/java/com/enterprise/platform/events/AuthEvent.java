package com.enterprise.platform.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEvent {

    private UUID eventId;

    private AuthEventType eventType;

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private LocalDateTime timestamp;

    private String details;
}