package com.enterprise.platform.project.repository;

import com.enterprise.platform.project.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Page<ProjectMember> findByProjectId(UUID projectId, Pageable pageable);

    List<ProjectMember> findByProjectId(UUID projectId);

    long countByProjectId(UUID projectId);

    List<ProjectMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
