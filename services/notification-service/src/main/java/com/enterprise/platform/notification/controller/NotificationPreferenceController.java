package com.enterprise.platform.notification.controller;

import com.enterprise.platform.notification.dto.request.UpdatePreferenceRequest;
import com.enterprise.platform.notification.dto.response.ApiResponse;
import com.enterprise.platform.notification.dto.response.NotificationPreferenceResponse;
import com.enterprise.platform.notification.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Endpoints for managing user delivery channels and event preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @PathVariable UUID userId) {
        NotificationPreferenceResponse response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @PathVariable UUID userId,
            @RequestBody UpdatePreferenceRequest request) {
        NotificationPreferenceResponse response = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification preferences updated successfully"));
    }
}
