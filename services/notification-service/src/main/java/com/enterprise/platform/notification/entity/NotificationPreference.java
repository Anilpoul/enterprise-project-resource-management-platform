package com.enterprise.platform.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "task_notifications", nullable = false)
    @Builder.Default
    private Boolean taskNotifications = true;

    @Column(name = "sprint_notifications", nullable = false)
    @Builder.Default
    private Boolean sprintNotifications = true;

    @Column(name = "resource_notifications", nullable = false)
    @Builder.Default
    private Boolean resourceNotifications = true;
}
