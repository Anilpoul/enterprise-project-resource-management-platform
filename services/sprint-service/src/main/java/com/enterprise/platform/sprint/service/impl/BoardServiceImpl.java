package com.enterprise.platform.sprint.service.impl;

import com.enterprise.platform.sprint.context.TenantContext;
import com.enterprise.platform.sprint.dto.request.CreateBoardColumnRequest;
import com.enterprise.platform.sprint.dto.request.CreateBoardRequest;
import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.dto.response.BoardResponse;
import com.enterprise.platform.sprint.entity.Board;
import com.enterprise.platform.sprint.entity.BoardColumn;
import com.enterprise.platform.sprint.exception.BadRequestException;
import com.enterprise.platform.sprint.exception.ResourceNotFoundException;
import com.enterprise.platform.sprint.mapper.BoardColumnMapper;
import com.enterprise.platform.sprint.mapper.BoardMapper;
import com.enterprise.platform.sprint.repository.BoardColumnRepository;
import com.enterprise.platform.sprint.repository.BoardRepository;
import com.enterprise.platform.sprint.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final BoardMapper boardMapper;
    private final BoardColumnMapper columnMapper;

    @Override
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Creating board '{}' for project: {} in organization: {}", request.getName(), request.getProjectId(), organizationId);

        Board board = Board.builder()
                .organizationId(organizationId)
                .projectId(request.getProjectId())
                .name(request.getName().trim())
                .description(request.getDescription())
                .boardType(request.getBoardType())
                .columns(new ArrayList<>())
                .build();

        Board savedBoard = boardRepository.save(board);

        // Seed default standard agile workflow columns
        List<BoardColumn> defaultColumns = List.of(
                BoardColumn.builder().board(savedBoard).organizationId(organizationId).name("Backlog").taskStatus("BACKLOG").columnOrder(0).build(),
                BoardColumn.builder().board(savedBoard).organizationId(organizationId).name("To Do").taskStatus("TODO").columnOrder(1).build(),
                BoardColumn.builder().board(savedBoard).organizationId(organizationId).name("In Progress").taskStatus("IN_PROGRESS").columnOrder(2).build(),
                BoardColumn.builder().board(savedBoard).organizationId(organizationId).name("In Review").taskStatus("IN_REVIEW").columnOrder(3).build(),
                BoardColumn.builder().board(savedBoard).organizationId(organizationId).name("Done").taskStatus("DONE").columnOrder(4).build()
        );

        List<BoardColumn> savedColumns = columnRepository.saveAll(defaultColumns);
        savedBoard.setColumns(new ArrayList<>(savedColumns));
        log.info("Board created with ID: {} and {} default columns", savedBoard.getId(), savedColumns.size());

        return boardMapper.toResponse(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByProject(UUID projectId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching boards for project: {}", projectId);

        return boardRepository.findByOrganizationIdAndProjectId(organizationId, projectId).stream()
                .map(boardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoardById(UUID boardId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching board by ID: {}", boardId);

        Board board = findBoardOrThrow(boardId, organizationId);
        return boardMapper.toResponse(board);
    }

    @Override
    @Transactional
    public BoardColumnResponse addColumn(UUID boardId, CreateBoardColumnRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Adding column '{}' to board: {}", request.getName(), boardId);

        Board board = findBoardOrThrow(boardId, organizationId);

        BoardColumn column = BoardColumn.builder()
                .board(board)
                .organizationId(organizationId)
                .name(request.getName().trim())
                .taskStatus(request.getTaskStatus().trim().toUpperCase())
                .columnOrder(request.getColumnOrder() != null ? request.getColumnOrder() : board.getColumns().size())
                .wipLimit(request.getWipLimit())
                .build();

        BoardColumn saved = columnRepository.save(column);
        log.info("Column added with ID: {}", saved.getId());

        return columnMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteColumn(UUID boardId, UUID columnId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting column: {} from board: {}", columnId, boardId);
        findBoardOrThrow(boardId, organizationId);

        BoardColumn column = columnRepository.findByIdAndBoardId(columnId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found with ID: " + columnId));

        columnRepository.delete(column);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID boardId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting board ID: {}", boardId);

        Board board = findBoardOrThrow(boardId, organizationId);
        boardRepository.delete(board);
    }

    private Board findBoardOrThrow(UUID boardId, UUID organizationId) {
        return boardRepository.findByIdAndOrganizationId(boardId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with ID: " + boardId));
    }

    private UUID getRequiredOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }
}
