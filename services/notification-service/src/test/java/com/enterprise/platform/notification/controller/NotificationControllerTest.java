package com.enterprise.platform.notification.controller;

import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.dto.response.NotificationResponse;
import com.enterprise.platform.notification.dto.response.PagedResponse;
import com.enterprise.platform.notification.dto.response.UnreadCountResponse;
import com.enterprise.platform.notification.exception.GlobalExceptionHandler;
import com.enterprise.platform.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private ObjectMapper objectMapper;
    private UUID recipientId;
    private UUID notificationId;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        recipientId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        notificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .organizationId(UUID.randomUUID())
                .recipientId(recipientId)
                .title("Task Assigned")
                .message("You were assigned a task")
                .notificationType(NotificationType.TASK_ASSIGNED)
                .channel(NotificationChannel.IN_APP)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/notifications should return 200 and paged response")
    void getNotifications() throws Exception {
        PagedResponse<NotificationResponse> pagedResponse = PagedResponse.<NotificationResponse>builder()
                .content(List.of(notificationResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(notificationService.getNotifications(eq(recipientId), any(), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("recipientId", recipientId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Task Assigned"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count should return 200 and count")
    void getUnreadCount() throws Exception {
        UnreadCountResponse unreadCount = UnreadCountResponse.builder()
                .recipientId(recipientId)
                .unreadCount(3L)
                .build();

        when(notificationService.getUnreadCount(recipientId)).thenReturn(unreadCount);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .param("recipientId", recipientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("POST /api/v1/notifications should return 201 and created notification")
    void createNotification() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .recipientId(recipientId)
                .title("New Alert")
                .message("Alert message body")
                .notificationType(NotificationType.SYSTEM_ALERT)
                .channel(NotificationChannel.IN_APP)
                .build();

        when(notificationService.createNotification(any(CreateNotificationRequest.class)))
                .thenReturn(notificationResponse);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notificationId.toString()));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read should mark notification as read")
    void markAsRead() throws Exception {
        notificationResponse.setIsRead(true);
        when(notificationService.markAsRead(notificationId)).thenReturn(notificationResponse);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/mark-all-read should return 200")
    void markAllAsRead() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/mark-all-read")
                        .param("recipientId", recipientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead(recipientId);
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} should return 200")
    void deleteNotification() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/all should return 200")
    void deleteAllNotifications() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/all")
                        .param("recipientId", recipientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).deleteAllNotifications(recipientId);
    }
}
