package com.enterprise.platform.notification.dto.response;

import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;

    private UUID organizationId;

    private UUID recipientId;

    private UUID senderId;

    private String title;

    private String message;

    private NotificationType notificationType;

    private UUID referenceId;

    private String referenceType;

    private NotificationChannel channel;

    private Boolean isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
