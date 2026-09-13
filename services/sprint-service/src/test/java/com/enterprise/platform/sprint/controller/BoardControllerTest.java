package com.enterprise.platform.sprint.controller;

import com.enterprise.platform.sprint.constants.enums.BoardType;
import com.enterprise.platform.sprint.dto.request.CreateBoardColumnRequest;
import com.enterprise.platform.sprint.dto.request.CreateBoardRequest;
import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.dto.response.BoardResponse;
import com.enterprise.platform.sprint.exception.GlobalExceptionHandler;
import com.enterprise.platform.sprint.exception.ResourceNotFoundException;
import com.enterprise.platform.sprint.service.BoardService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private BoardController boardController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID boardId;
    private UUID projectId;
    private BoardResponse boardResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        boardId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        boardResponse = BoardResponse.builder()
                .id(boardId)
                .organizationId(UUID.randomUUID())
                .projectId(projectId)
                .name("Core Scrum Board")
                .description("Main board")
                .boardType(BoardType.SCRUM)
                .columns(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/boards - Success returns 201 CREATED")
    void testCreateBoard_Success() throws Exception {
        CreateBoardRequest request = CreateBoardRequest.builder()
                .projectId(projectId)
                .name("Core Scrum Board")
                .boardType(BoardType.SCRUM)
                .build();

        when(boardService.createBoard(any(CreateBoardRequest.class))).thenReturn(boardResponse);

        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Core Scrum Board"));
    }

    @Test
    @DisplayName("POST /api/v1/boards - Validation failure returns 400")
    void testCreateBoard_ValidationFailure() throws Exception {
        CreateBoardRequest request = CreateBoardRequest.builder()
                .projectId(null)
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/boards - Success returns 200 list")
    void testGetBoardsByProject_Success() throws Exception {
        when(boardService.getBoardsByProject(projectId)).thenReturn(List.of(boardResponse));

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Core Scrum Board"));
    }

    @Test
    @DisplayName("GET /api/v1/boards/{id} - Success returns 200 OK")
    void testGetBoardById_Success() throws Exception {
        when(boardService.getBoardById(boardId)).thenReturn(boardResponse);

        mockMvc.perform(get("/api/v1/boards/{id}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(boardId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/boards/{id} - Not found returns 404")
    void testGetBoardById_NotFound() throws Exception {
        when(boardService.getBoardById(boardId))
                .thenThrow(new ResourceNotFoundException("Board not found with ID: " + boardId));

        mockMvc.perform(get("/api/v1/boards/{id}", boardId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/boards/{id}/columns - Success returns 201 CREATED")
    void testAddColumn_Success() throws Exception {
        CreateBoardColumnRequest request = CreateBoardColumnRequest.builder()
                .name("QA Testing")
                .taskStatus("IN_TEST")
                .columnOrder(5)
                .build();

        BoardColumnResponse columnResponse = BoardColumnResponse.builder()
                .id(UUID.randomUUID())
                .boardId(boardId)
                .name("QA Testing")
                .taskStatus("IN_TEST")
                .columnOrder(5)
                .build();

        when(boardService.addColumn(eq(boardId), any(CreateBoardColumnRequest.class))).thenReturn(columnResponse);

        mockMvc.perform(post("/api/v1/boards/{id}/columns", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("QA Testing"));
    }

    @Test
    @DisplayName("DELETE /api/v1/boards/{boardId}/columns/{columnId} - Success returns 200 OK")
    void testDeleteColumn_Success() throws Exception {
        UUID columnId = UUID.randomUUID();
        doNothing().when(boardService).deleteColumn(boardId, columnId);

        mockMvc.perform(delete("/api/v1/boards/{boardId}/columns/{columnId}", boardId, columnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/boards/{id} - Success returns 200 OK")
    void testDeleteBoard_Success() throws Exception {
        doNothing().when(boardService).deleteBoard(boardId);

        mockMvc.perform(delete("/api/v1/boards/{id}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
