package com.enterprise.platform.notification.service.impl;

import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.context.TenantContext;
import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.dto.response.NotificationResponse;
import com.enterprise.platform.notification.dto.response.PagedResponse;
import com.enterprise.platform.notification.dto.response.UnreadCountResponse;
import com.enterprise.platform.notification.entity.Notification;
import com.enterprise.platform.notification.exception.BadRequestException;
import com.enterprise.platform.notification.exception.ResourceNotFoundException;
import com.enterprise.platform.notification.mapper.NotificationMapper;
import com.enterprise.platform.notification.repository.NotificationRepository;
import com.enterprise.platform.notification.service.NotificationPreferenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationPreferenceService preferenceService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID organizationId;
    private UUID recipientId;
    private UUID notificationId;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private CreateNotificationRequest createRequest;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        TenantContext.setOrganizationId(organizationId);

        notification = Notification.builder()
                .id(notificationId)
                .organizationId(organizationId)
                .recipientId(recipientId)
                .title("Task Assigned")
                .message("You have a new task")
                .notificationType(NotificationType.TASK_ASSIGNED)
                .channel(NotificationChannel.IN_APP)
                .isRead(false)
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .organizationId(organizationId)
                .recipientId(recipientId)
                .title("Task Assigned")
                .message("You have a new task")
                .notificationType(NotificationType.TASK_ASSIGNED)
                .channel(NotificationChannel.IN_APP)
                .isRead(false)
                .build();

        createRequest = CreateNotificationRequest.builder()
                .recipientId(recipientId)
                .title("Task Assigned")
                .message("You have a new task")
                .notificationType(NotificationType.TASK_ASSIGNED)
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("getNotifications should return paged notifications when isRead is null")
    void getNotifications_all() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(notificationRepository.findByOrganizationIdAndRecipientId(organizationId, recipientId, pageable))
                .thenReturn(page);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        PagedResponse<NotificationResponse> result = notificationService.getNotifications(recipientId, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(notificationRepository).findByOrganizationIdAndRecipientId(organizationId, recipientId, pageable);
    }

    @Test
    @DisplayName("getNotifications should filter by isRead when isRead is provided")
    void getNotifications_filtered() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(notificationRepository.findByOrganizationIdAndRecipientIdAndIsRead(organizationId, recipientId, false, pageable))
                .thenReturn(page);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        PagedResponse<NotificationResponse> result = notificationService.getNotifications(recipientId, false, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findByOrganizationIdAndRecipientIdAndIsRead(organizationId, recipientId, false, pageable);
    }

    @Test
    @DisplayName("getUnreadCount should return unread notification count")
    void getUnreadCount_success() {
        when(notificationRepository.countByOrganizationIdAndRecipientIdAndIsReadFalse(organizationId, recipientId))
                .thenReturn(5L);

        UnreadCountResponse result = notificationService.getUnreadCount(recipientId);

        assertThat(result).isNotNull();
        assertThat(result.getRecipientId()).isEqualTo(recipientId);
        assertThat(result.getUnreadCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAsRead should set isRead true and save")
    void markAsRead_success() {
        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(notificationId);

        assertThat(result).isNotNull();
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead should throw ResourceNotFoundException when notification not found")
    void markAsRead_notFound() {
        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID");
    }

    @Test
    @DisplayName("markAllAsRead should invoke repository markAllAsRead")
    void markAllAsRead_success() {
        when(notificationRepository.markAllAsRead(eq(organizationId), eq(recipientId), any(LocalDateTime.class)))
                .thenReturn(3);

        notificationService.markAllAsRead(recipientId);

        verify(notificationRepository).markAllAsRead(eq(organizationId), eq(recipientId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("createNotification should save notification when preferences allow")
    void createNotification_allowed() {
        when(preferenceService.isNotificationEnabled(organizationId, recipientId, NotificationType.TASK_ASSIGNED, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(notificationMapper.toEntity(createRequest)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(createRequest);

        assertThat(result).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("createNotification should return null and skip save when preferences disallow")
    void createNotification_disallowed() {
        when(preferenceService.isNotificationEnabled(organizationId, recipientId, NotificationType.TASK_ASSIGNED, NotificationChannel.IN_APP))
                .thenReturn(false);

        NotificationResponse result = notificationService.createNotification(createRequest);

        assertThat(result).isNull();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteNotification should delete from repository when found")
    void deleteNotification_success() {
        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId);

        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("deleteNotification should throw ResourceNotFoundException when not found")
    void deleteNotification_notFound() {
        when(notificationRepository.findByIdAndOrganizationId(notificationId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteAllNotifications should call repository deleteAllByRecipient")
    void deleteAllNotifications_success() {
        when(notificationRepository.deleteAllByRecipient(organizationId, recipientId))
                .thenReturn(4);

        notificationService.deleteAllNotifications(recipientId);

        verify(notificationRepository).deleteAllByRecipient(organizationId, recipientId);
    }

    @Test
    @DisplayName("should throw BadRequestException when organization header is missing")
    void missingOrganizationHeader_throwsBadRequest() {
        TenantContext.clear();

        assertThatThrownBy(() -> notificationService.getUnreadCount(recipientId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }
}
