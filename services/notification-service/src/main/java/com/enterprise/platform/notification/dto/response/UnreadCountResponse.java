package com.enterprise.platform.notification.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {

    private UUID recipientId;

    private long unreadCount;
}
