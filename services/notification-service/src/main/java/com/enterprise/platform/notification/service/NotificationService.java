package com.enterprise.platform.notification.service;

import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.dto.response.NotificationResponse;
import com.enterprise.platform.notification.dto.response.PagedResponse;
import com.enterprise.platform.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    PagedResponse<NotificationResponse> getNotifications(UUID recipientId, Boolean isRead, Pageable pageable);

    UnreadCountResponse getUnreadCount(UUID recipientId);

    NotificationResponse markAsRead(UUID id);

    void markAllAsRead(UUID recipientId);

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse createInternalNotification(UUID organizationId, CreateNotificationRequest request);

    void deleteNotification(UUID id);

    void deleteAllNotifications(UUID recipientId);
}
