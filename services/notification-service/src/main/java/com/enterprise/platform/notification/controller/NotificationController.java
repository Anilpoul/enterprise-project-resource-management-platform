package com.enterprise.platform.notification.controller;

import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.dto.response.ApiResponse;
import com.enterprise.platform.notification.dto.response.NotificationResponse;
import com.enterprise.platform.notification.dto.response.PagedResponse;
import com.enterprise.platform.notification.dto.response.UnreadCountResponse;
import com.enterprise.platform.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications and alerts")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications with pagination and optional read filter")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @RequestParam UUID recipientId,
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<NotificationResponse> response = notificationService.getNotifications(recipientId, isRead, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count for a user")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @RequestParam UUID recipientId) {
        UnreadCountResponse response = notificationService.getUnreadCount(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create an in-app or multi-channel notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Notification created successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read for a recipient")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam UUID recipientId) {
        notificationService.markAllAsRead(recipientId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all notifications for a recipient")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            @RequestParam UUID recipientId) {
        notificationService.deleteAllNotifications(recipientId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications deleted successfully"));
    }
}
