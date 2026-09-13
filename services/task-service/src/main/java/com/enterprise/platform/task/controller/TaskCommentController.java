package com.enterprise.platform.task.controller;

import com.enterprise.platform.task.dto.request.CreateTaskCommentRequest;
import com.enterprise.platform.task.dto.response.ApiResponse;
import com.enterprise.platform.task.dto.response.TaskCommentResponse;
import com.enterprise.platform.task.service.TaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Task Comment Management", description = "APIs for managing task activity and discussions")
public class TaskCommentController {

    private final TaskCommentService commentService;

    @PostMapping
    @Operation(summary = "Add a comment to task")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentRequest request
    ) {
        log.info("REST request to add comment to task {}", taskId);
        TaskCommentResponse response = commentService.addComment(taskId, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Comment added successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get all comments for a task")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(
            @PathVariable UUID taskId
    ) {
        log.info("REST request to get comments for task {}", taskId);
        List<TaskCommentResponse> response = commentService.getComments(taskId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId
    ) {
        log.info("REST request to delete comment {} from task {}", commentId, taskId);
        commentService.deleteComment(taskId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }
}
