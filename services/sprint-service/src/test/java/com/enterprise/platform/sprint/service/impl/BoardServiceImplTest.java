package com.enterprise.platform.sprint.service.impl;

import com.enterprise.platform.sprint.constants.enums.BoardType;
import com.enterprise.platform.sprint.context.TenantContext;
import com.enterprise.platform.sprint.dto.request.CreateBoardColumnRequest;
import com.enterprise.platform.sprint.dto.request.CreateBoardRequest;
import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.dto.response.BoardResponse;
import com.enterprise.platform.sprint.entity.Board;
import com.enterprise.platform.sprint.entity.BoardColumn;
import com.enterprise.platform.sprint.exception.ResourceNotFoundException;
import com.enterprise.platform.sprint.mapper.BoardColumnMapper;
import com.enterprise.platform.sprint.mapper.BoardMapper;
import com.enterprise.platform.sprint.repository.BoardColumnRepository;
import com.enterprise.platform.sprint.repository.BoardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private BoardColumnMapper columnMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    private UUID organizationId;
    private UUID projectId;
    private UUID boardId;
    private Board board;
    private BoardResponse boardResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        boardId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        board = Board.builder()
                .id(boardId)
                .organizationId(organizationId)
                .projectId(projectId)
                .name("Core Scrum Board")
                .description("Main scrum board")
                .boardType(BoardType.SCRUM)
                .columns(new ArrayList<>())
                .build();

        boardResponse = BoardResponse.builder()
                .id(boardId)
                .organizationId(organizationId)
                .projectId(projectId)
                .name("Core Scrum Board")
                .boardType(BoardType.SCRUM)
                .columns(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create board and seed default 5 agile columns")
    void testCreateBoard_Success() {
        CreateBoardRequest request = CreateBoardRequest.builder()
                .projectId(projectId)
                .name("Core Scrum Board")
                .description("Main scrum board")
                .boardType(BoardType.SCRUM)
                .build();

        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(columnRepository.saveAll(anyList())).thenReturn(List.of());
        when(boardMapper.toResponse(board)).thenReturn(boardResponse);

        BoardResponse response = boardService.createBoard(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Core Scrum Board");
        verify(boardRepository).save(any(Board.class));
        verify(columnRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should get boards by project")
    void testGetBoardsByProject_Success() {
        when(boardRepository.findByOrganizationIdAndProjectId(organizationId, projectId)).thenReturn(List.of(board));
        when(boardMapper.toResponse(board)).thenReturn(boardResponse);

        List<BoardResponse> list = boardService.getBoardsByProject(projectId);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Should get board by ID")
    void testGetBoardById_Success() {
        when(boardRepository.findByIdAndOrganizationId(boardId, organizationId)).thenReturn(Optional.of(board));
        when(boardMapper.toResponse(board)).thenReturn(boardResponse);

        BoardResponse response = boardService.getBoardById(boardId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(boardId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when board not found")
    void testGetBoardById_NotFound() {
        when(boardRepository.findByIdAndOrganizationId(boardId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardById(boardId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Board not found");
    }

    @Test
    @DisplayName("Should add column to board successfully")
    void testAddColumn_Success() {
        CreateBoardColumnRequest request = CreateBoardColumnRequest.builder()
                .name("QA Testing")
                .taskStatus("IN_TEST")
                .columnOrder(5)
                .wipLimit(3)
                .build();

        BoardColumn column = BoardColumn.builder()
                .id(UUID.randomUUID())
                .board(board)
                .name("QA Testing")
                .taskStatus("IN_TEST")
                .columnOrder(5)
                .wipLimit(3)
                .build();

        BoardColumnResponse columnResponse = BoardColumnResponse.builder()
                .id(column.getId())
                .boardId(boardId)
                .name("QA Testing")
                .taskStatus("IN_TEST")
                .columnOrder(5)
                .wipLimit(3)
                .build();

        when(boardRepository.findByIdAndOrganizationId(boardId, organizationId)).thenReturn(Optional.of(board));
        when(columnRepository.save(any(BoardColumn.class))).thenReturn(column);
        when(columnMapper.toResponse(column)).thenReturn(columnResponse);

        BoardColumnResponse response = boardService.addColumn(boardId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("QA Testing");
        verify(columnRepository).save(any(BoardColumn.class));
    }

    @Test
    @DisplayName("Should delete column successfully")
    void testDeleteColumn_Success() {
        UUID columnId = UUID.randomUUID();
        BoardColumn column = BoardColumn.builder().id(columnId).board(board).build();

        when(boardRepository.findByIdAndOrganizationId(boardId, organizationId)).thenReturn(Optional.of(board));
        when(columnRepository.findByIdAndBoardId(columnId, boardId)).thenReturn(Optional.of(column));

        boardService.deleteColumn(boardId, columnId);

        verify(columnRepository).delete(column);
    }

    @Test
    @DisplayName("Should delete board successfully")
    void testDeleteBoard_Success() {
        when(boardRepository.findByIdAndOrganizationId(boardId, organizationId)).thenReturn(Optional.of(board));

        boardService.deleteBoard(boardId);

        verify(boardRepository).delete(board);
    }
}
