package com.enterprise.platform.sprint.repository;

import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import com.enterprise.platform.sprint.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Optional<Sprint> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<Sprint> findByOrganizationIdAndProjectIdOrderByCreatedAtDesc(UUID organizationId, UUID projectId);

    List<Sprint> findByOrganizationIdAndProjectIdAndStatus(UUID organizationId, UUID projectId, SprintStatus status);

    Optional<Sprint> findByProjectIdAndStatus(UUID projectId, SprintStatus status);

    boolean existsByProjectIdAndStatus(UUID projectId, SprintStatus status);
}
