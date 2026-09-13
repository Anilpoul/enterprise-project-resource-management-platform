package com.enterprise.platform.notification.service;

import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.dto.request.UpdatePreferenceRequest;
import com.enterprise.platform.notification.dto.response.NotificationPreferenceResponse;

import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse getPreferences(UUID userId);

    NotificationPreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request);

    boolean isNotificationEnabled(UUID organizationId, UUID userId, NotificationType notificationType, NotificationChannel channel);
}
