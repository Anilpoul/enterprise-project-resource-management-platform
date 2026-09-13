package com.enterprise.platform.notification.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private UUID id;

    private UUID organizationId;

    private UUID userId;

    private Boolean emailEnabled;

    private Boolean inAppEnabled;

    private Boolean taskNotifications;

    private Boolean sprintNotifications;

    private Boolean resourceNotifications;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
