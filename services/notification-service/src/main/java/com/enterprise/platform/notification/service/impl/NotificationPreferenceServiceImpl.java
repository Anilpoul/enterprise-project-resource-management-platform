package com.enterprise.platform.notification.service.impl;

import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.context.TenantContext;
import com.enterprise.platform.notification.dto.request.UpdatePreferenceRequest;
import com.enterprise.platform.notification.dto.response.NotificationPreferenceResponse;
import com.enterprise.platform.notification.entity.NotificationPreference;
import com.enterprise.platform.notification.exception.BadRequestException;
import com.enterprise.platform.notification.mapper.NotificationPreferenceMapper;
import com.enterprise.platform.notification.repository.NotificationPreferenceRepository;
import com.enterprise.platform.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationPreferenceMapper preferenceMapper;

    @Override
    @Transactional
    public NotificationPreferenceResponse getPreferences(UUID userId) {
        UUID orgId = validateOrganizationId();
        NotificationPreference preference = preferenceRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseGet(() -> createDefaultPreference(orgId, userId));
        return preferenceMapper.toResponse(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request) {
        UUID orgId = validateOrganizationId();
        NotificationPreference preference = preferenceRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseGet(() -> createDefaultPreference(orgId, userId));

        preferenceMapper.updateEntityFromRequest(request, preference);
        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("Updated notification preferences for user {} in org {}", userId, orgId);
        return preferenceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(UUID organizationId, UUID userId, NotificationType notificationType, NotificationChannel channel) {
        if (organizationId == null || userId == null) {
            return false;
        }

        NotificationPreference pref = preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElse(null);

        // If no preference saved yet, default is everything enabled
        if (pref == null) {
            return true;
        }

        // Check channel level
        if (channel == NotificationChannel.EMAIL && Boolean.FALSE.equals(pref.getEmailEnabled())) {
            return false;
        }
        if (channel == NotificationChannel.IN_APP && Boolean.FALSE.equals(pref.getInAppEnabled())) {
            return false;
        }

        // Check type category level
        if (notificationType != null) {
            return switch (notificationType) {
                case TASK_ASSIGNED, TASK_STATUS_CHANGED, TASK_CREATED, TASK_UPDATED, TASK_COMMENT_ADDED ->
                        !Boolean.FALSE.equals(pref.getTaskNotifications());
                case SPRINT_STARTED, SPRINT_COMPLETED ->
                        !Boolean.FALSE.equals(pref.getSprintNotifications());
                case RESOURCE_ALLOCATED, RESOURCE_OVERALLOCATED ->
                        !Boolean.FALSE.equals(pref.getResourceNotifications());
                case SYSTEM_ALERT -> true;
            };
        }

        return true;
    }

    private NotificationPreference createDefaultPreference(UUID orgId, UUID userId) {
        NotificationPreference defaultPref = NotificationPreference.builder()
                .organizationId(orgId)
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .taskNotifications(true)
                .sprintNotifications(true)
                .resourceNotifications(true)
                .build();
        return preferenceRepository.save(defaultPref);
    }

    private UUID validateOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }
}
