package com.enterprise.platform.notification.service.impl;

import com.enterprise.platform.notification.constants.NotificationChannel;
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
import com.enterprise.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationPreferenceService preferenceService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(UUID recipientId, Boolean isRead, Pageable pageable) {
        UUID orgId = validateOrganizationId();
        Page<Notification> page;

        if (isRead != null) {
            page = notificationRepository.findByOrganizationIdAndRecipientIdAndIsRead(orgId, recipientId, isRead, pageable);
        } else {
            page = notificationRepository.findByOrganizationIdAndRecipientId(orgId, recipientId, pageable);
        }

        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID recipientId) {
        UUID orgId = validateOrganizationId();
        long unreadCount = notificationRepository.countByOrganizationIdAndRecipientIdAndIsReadFalse(orgId, recipientId);
        return UnreadCountResponse.builder()
                .recipientId(recipientId)
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        UUID orgId = validateOrganizationId();
        Notification notification = notificationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Marked notification {} as read for org {}", id, orgId);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        UUID orgId = validateOrganizationId();
        int updatedCount = notificationRepository.markAllAsRead(orgId, recipientId, LocalDateTime.now());
        log.info("Marked {} notifications as read for recipient {} in org {}", updatedCount, recipientId, orgId);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        UUID orgId = validateOrganizationId();
        return createInternalNotification(orgId, request);
    }

    @Override
    @Transactional
    public NotificationResponse createInternalNotification(UUID organizationId, CreateNotificationRequest request) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required for creating notification");
        }

        NotificationChannel channel = request.getChannel() != null ? request.getChannel() : NotificationChannel.IN_APP;

        boolean enabled = preferenceService.isNotificationEnabled(
                organizationId,
                request.getRecipientId(),
                request.getNotificationType(),
                channel
        );

        if (!enabled) {
            log.info("Notification discarded due to user preferences: user={}, type={}, channel={}",
                    request.getRecipientId(), request.getNotificationType(), channel);
            return null;
        }

        Notification notification = notificationMapper.toEntity(request);
        notification.setOrganizationId(organizationId);
        notification.setChannel(channel);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification {} of type {} for recipient {}",
                saved.getId(), saved.getNotificationType(), saved.getRecipientId());

        if (channel == NotificationChannel.EMAIL) {
            simulateEmailDelivery(saved);
        }

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        UUID orgId = validateOrganizationId();
        Notification notification = notificationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notificationRepository.delete(notification);
        log.info("Deleted notification {} in org {}", id, orgId);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(UUID recipientId) {
        UUID orgId = validateOrganizationId();
        int deletedCount = notificationRepository.deleteAllByRecipient(orgId, recipientId);
        log.info("Deleted {} notifications for recipient {} in org {}", deletedCount, recipientId, orgId);
    }

    private void simulateEmailDelivery(Notification notification) {
        log.info("[EMAIL DISPATCH STUB] Sending email to user {} with title: '{}' and message: '{}'",
                notification.getRecipientId(), notification.getTitle(), notification.getMessage());
    }

    private UUID validateOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }
}
