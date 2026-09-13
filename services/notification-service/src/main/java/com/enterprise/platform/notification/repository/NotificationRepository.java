package com.enterprise.platform.notification.repository;

import com.enterprise.platform.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByOrganizationIdAndRecipientId(UUID organizationId, UUID recipientId, Pageable pageable);

    Page<Notification> findByOrganizationIdAndRecipientIdAndIsRead(UUID organizationId, UUID recipientId, Boolean isRead, Pageable pageable);

    long countByOrganizationIdAndRecipientIdAndIsReadFalse(UUID organizationId, UUID recipientId);

    Optional<Notification> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.organizationId = :organizationId AND n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("organizationId") UUID organizationId, @Param("recipientId") UUID recipientId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.organizationId = :organizationId AND n.recipientId = :recipientId")
    int deleteAllByRecipient(@Param("organizationId") UUID organizationId, @Param("recipientId") UUID recipientId);
}
