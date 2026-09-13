package com.enterprise.platform.sprint.repository;

import com.enterprise.platform.sprint.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {

    Optional<Board> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<Board> findByOrganizationIdAndProjectId(UUID organizationId, UUID projectId);
}
