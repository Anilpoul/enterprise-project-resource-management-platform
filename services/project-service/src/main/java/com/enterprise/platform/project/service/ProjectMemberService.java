package com.enterprise.platform.project.service;

import com.enterprise.platform.project.dto.request.AddProjectMemberRequest;
import com.enterprise.platform.project.dto.request.UpdateProjectMemberRoleRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectMemberResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectMemberService {

    ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request);

    PagedResponse<ProjectMemberResponse> getMembers(UUID projectId, Pageable pageable);

    ProjectMemberResponse getMember(UUID projectId, UUID userId);

    ProjectMemberResponse updateMemberRole(UUID projectId, UUID userId, UpdateProjectMemberRoleRequest request);

    void removeMember(UUID projectId, UUID userId);
}
