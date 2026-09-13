package com.enterprise.platform.project.repository;

import com.enterprise.platform.project.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, UUID> {

    List<ProjectMilestone> findByProjectId(UUID projectId);

    Optional<ProjectMilestone> findByIdAndProjectId(UUID id, UUID projectId);
}
