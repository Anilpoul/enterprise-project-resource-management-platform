package com.enterprise.platform.project.service;

import com.enterprise.platform.project.dto.request.CreateMilestoneRequest;
import com.enterprise.platform.project.dto.request.UpdateMilestoneRequest;
import com.enterprise.platform.project.dto.response.ProjectMilestoneResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectMilestoneService {

    ProjectMilestoneResponse createMilestone(UUID projectId, CreateMilestoneRequest request);

    List<ProjectMilestoneResponse> getMilestones(UUID projectId);

    ProjectMilestoneResponse updateMilestone(UUID projectId, UUID milestoneId, UpdateMilestoneRequest request);

    void deleteMilestone(UUID projectId, UUID milestoneId);
}
