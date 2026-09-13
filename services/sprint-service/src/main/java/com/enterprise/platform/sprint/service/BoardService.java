package com.enterprise.platform.sprint.service;

import com.enterprise.platform.sprint.dto.request.CreateBoardColumnRequest;
import com.enterprise.platform.sprint.dto.request.CreateBoardRequest;
import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.dto.response.BoardResponse;

import java.util.List;
import java.util.UUID;

public interface BoardService {

    BoardResponse createBoard(CreateBoardRequest request);

    List<BoardResponse> getBoardsByProject(UUID projectId);

    BoardResponse getBoardById(UUID boardId);

    BoardColumnResponse addColumn(UUID boardId, CreateBoardColumnRequest request);

    void deleteColumn(UUID boardId, UUID columnId);

    void deleteBoard(UUID boardId);
}
