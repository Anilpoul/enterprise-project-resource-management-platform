package com.enterprise.platform.sprint.controller;

import com.enterprise.platform.sprint.dto.request.CreateBoardColumnRequest;
import com.enterprise.platform.sprint.dto.request.CreateBoardRequest;
import com.enterprise.platform.sprint.dto.response.ApiResponse;
import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.dto.response.BoardResponse;
import com.enterprise.platform.sprint.service.BoardService;
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
@RequiredArgsConstructor
@Tag(name = "Agile Board Management", description = "APIs for Scrum and Kanban Boards and Workflow Columns")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/api/v1/boards")
    @Operation(summary = "Create an agile board with standard columns")
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @Valid @RequestBody CreateBoardRequest request
    ) {
        log.info("REST request to create board: '{}' for project: {}", request.getName(), request.getProjectId());
        BoardResponse response = boardService.createBoard(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Board created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/projects/{projectId}/boards")
    @Operation(summary = "List boards for project")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoardsByProject(
            @PathVariable UUID projectId
    ) {
        log.info("REST request to get boards for project: {}", projectId);
        List<BoardResponse> response = boardService.getBoardsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/boards/{id}")
    @Operation(summary = "Get board by ID with columns")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardById(
            @PathVariable UUID id
    ) {
        log.info("REST request to get board by ID: {}", id);
        BoardResponse response = boardService.getBoardById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/boards/{id}/columns")
    @Operation(summary = "Add custom column to board")
    public ResponseEntity<ApiResponse<BoardColumnResponse>> addColumn(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBoardColumnRequest request
    ) {
        log.info("REST request to add column to board: {}", id);
        BoardColumnResponse response = boardService.addColumn(id, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Column added successfully"),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/api/v1/boards/{boardId}/columns/{columnId}")
    @Operation(summary = "Delete column from board")
    public ResponseEntity<ApiResponse<Void>> deleteColumn(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId
    ) {
        log.info("REST request to delete column {} from board {}", columnId, boardId);
        boardService.deleteColumn(boardId, columnId);
        return ResponseEntity.ok(ApiResponse.success(null, "Column deleted successfully"));
    }

    @DeleteMapping("/api/v1/boards/{id}")
    @Operation(summary = "Delete board")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete board ID: {}", id);
        boardService.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Board deleted successfully"));
    }
}
