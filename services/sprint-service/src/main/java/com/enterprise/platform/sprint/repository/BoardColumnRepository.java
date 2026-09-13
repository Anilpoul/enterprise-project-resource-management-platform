package com.enterprise.platform.sprint.repository;

import com.enterprise.platform.sprint.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {

    List<BoardColumn> findByBoardIdOrderByColumnOrderAsc(UUID boardId);

    Optional<BoardColumn> findByIdAndBoardId(UUID id, UUID boardId);
}
