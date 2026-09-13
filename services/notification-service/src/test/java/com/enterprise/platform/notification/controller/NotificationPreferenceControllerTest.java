package com.enterprise.platform.notification.controller;

import com.enterprise.platform.notification.dto.request.UpdatePreferenceRequest;
import com.enterprise.platform.notification.dto.response.NotificationPreferenceResponse;
import com.enterprise.platform.notification.exception.GlobalExceptionHandler;
import com.enterprise.platform.notification.service.NotificationPreferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationPreferenceService preferenceService;

    @InjectMocks
    private NotificationPreferenceController preferenceController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private NotificationPreferenceResponse preferenceResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(preferenceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        userId = UUID.randomUUID();
        preferenceResponse = NotificationPreferenceResponse.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .taskNotifications(true)
                .sprintNotifications(true)
                .resourceNotifications(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/notifications/preferences/{userId} should return 200 and preferences")
    void getPreferences() throws Exception {
        when(preferenceService.getPreferences(userId)).thenReturn(preferenceResponse);

        mockMvc.perform(get("/api/v1/notifications/preferences/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.emailEnabled").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/preferences/{userId} should return 200 and updated preferences")
    void updatePreferences() throws Exception {
        UpdatePreferenceRequest request = UpdatePreferenceRequest.builder()
                .emailEnabled(false)
                .build();

        preferenceResponse.setEmailEnabled(false);
        when(preferenceService.updatePreferences(eq(userId), any(UpdatePreferenceRequest.class)))
                .thenReturn(preferenceResponse);

        mockMvc.perform(put("/api/v1/notifications/preferences/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailEnabled").value(false));
    }
}
