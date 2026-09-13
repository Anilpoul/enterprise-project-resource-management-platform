package com.enterprise.platform.task.controller;

import com.enterprise.platform.task.dto.request.CreateTaskCommentRequest;
import com.enterprise.platform.task.dto.response.TaskCommentResponse;
import com.enterprise.platform.task.exception.GlobalExceptionHandler;
import com.enterprise.platform.task.exception.ResourceNotFoundException;
import com.enterprise.platform.task.service.TaskCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskCommentService commentService;

    @InjectMocks
    private TaskCommentController commentController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID taskId;
    private UUID commentId;
    private TaskCommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        taskId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        commentResponse = TaskCommentResponse.builder()
                .id(commentId)
                .taskId(taskId)
                .organizationId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .comment("Great progress!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{taskId}/comments - Success returns 201 CREATED")
    void testAddComment_Success() throws Exception {
        CreateTaskCommentRequest request = new CreateTaskCommentRequest("Great progress!");

        when(commentService.addComment(eq(taskId), any(CreateTaskCommentRequest.class)))
                .thenReturn(commentResponse);

        mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.comment").value("Great progress!"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{taskId}/comments - Blank comment returns 400")
    void testAddComment_BlankComment() throws Exception {
        CreateTaskCommentRequest invalidRequest = new CreateTaskCommentRequest("");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{taskId}/comments - Success returns 200 list")
    void testGetComments_Success() throws Exception {
        when(commentService.getComments(taskId)).thenReturn(List.of(commentResponse));

        mockMvc.perform(get("/api/v1/tasks/{taskId}/comments", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].comment").value("Great progress!"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{taskId}/comments/{commentId} - Success returns 200")
    void testDeleteComment_Success() throws Exception {
        doNothing().when(commentService).deleteComment(taskId, commentId);

        mockMvc.perform(delete("/api/v1/tasks/{taskId}/comments/{commentId}", taskId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{taskId}/comments/{commentId} - Not found returns 404")
    void testDeleteComment_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Comment not found with ID: " + commentId))
                .when(commentService).deleteComment(taskId, commentId);

        mockMvc.perform(delete("/api/v1/tasks/{taskId}/comments/{commentId}", taskId, commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
