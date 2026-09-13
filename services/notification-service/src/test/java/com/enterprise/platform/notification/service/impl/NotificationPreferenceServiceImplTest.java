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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceImplTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationPreferenceMapper preferenceMapper;

    @InjectMocks
    private NotificationPreferenceServiceImpl preferenceService;

    private UUID organizationId;
    private UUID userId;
    private NotificationPreference preference;
    private NotificationPreferenceResponse preferenceResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        TenantContext.setOrganizationId(organizationId);

        preference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .taskNotifications(true)
                .sprintNotifications(true)
                .resourceNotifications(true)
                .build();

        preferenceResponse = NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .organizationId(organizationId)
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .taskNotifications(true)
                .sprintNotifications(true)
                .resourceNotifications(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("getPreferences should return existing preference when found")
    void getPreferences_existing() {
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(preference));
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.getPreferences(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPreferences should create default preference when none exists")
    void getPreferences_createsDefault() {
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.getPreferences(userId);

        assertThat(result).isNotNull();
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("updatePreferences should update existing preference")
    void updatePreferences_success() {
        UpdatePreferenceRequest updateRequest = UpdatePreferenceRequest.builder()
                .emailEnabled(false)
                .taskNotifications(true)
                .build();

        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(preference));
        doAnswer(inv -> {
            preference.setEmailEnabled(false);
            return null;
        }).when(preferenceMapper).updateEntityFromRequest(updateRequest, preference);
        when(preferenceRepository.save(preference)).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.updatePreferences(userId, updateRequest);

        assertThat(result).isNotNull();
        verify(preferenceRepository).save(preference);
    }

    @Test
    @DisplayName("isNotificationEnabled should return true when no preference entity exists (default enabled)")
    void isNotificationEnabled_noPrefExists() {
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.empty());

        boolean enabled = preferenceService.isNotificationEnabled(
                organizationId, userId, NotificationType.TASK_ASSIGNED, NotificationChannel.IN_APP);

        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("isNotificationEnabled should return false when email channel is disabled")
    void isNotificationEnabled_emailDisabled() {
        preference.setEmailEnabled(false);
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(preference));

        boolean enabled = preferenceService.isNotificationEnabled(
                organizationId, userId, NotificationType.TASK_ASSIGNED, NotificationChannel.EMAIL);

        assertThat(enabled).isFalse();
    }

    @Test
    @DisplayName("isNotificationEnabled should return false when task notifications category is disabled")
    void isNotificationEnabled_taskCategoryDisabled() {
        preference.setTaskNotifications(false);
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(preference));

        boolean enabled = preferenceService.isNotificationEnabled(
                organizationId, userId, NotificationType.TASK_ASSIGNED, NotificationChannel.IN_APP);

        assertThat(enabled).isFalse();
    }

    @Test
    @DisplayName("isNotificationEnabled should return true for SYSTEM_ALERT even if task/sprint/resource disabled")
    void isNotificationEnabled_systemAlertAlwaysEnabled() {
        preference.setTaskNotifications(false);
        preference.setSprintNotifications(false);
        preference.setResourceNotifications(false);
        when(preferenceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(preference));

        boolean enabled = preferenceService.isNotificationEnabled(
                organizationId, userId, NotificationType.SYSTEM_ALERT, NotificationChannel.IN_APP);

        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("should throw BadRequestException when organization header is missing")
    void missingOrganizationHeader_throwsBadRequest() {
        TenantContext.clear();

        assertThatThrownBy(() -> preferenceService.getPreferences(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }
}
