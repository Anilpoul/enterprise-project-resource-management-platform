package com.enterprise.platform.sprint.service;

import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import com.enterprise.platform.sprint.dto.request.CompleteSprintRequest;
import com.enterprise.platform.sprint.dto.request.CreateSprintRequest;
import com.enterprise.platform.sprint.dto.request.StartSprintRequest;
import com.enterprise.platform.sprint.dto.request.UpdateSprintRequest;
import com.enterprise.platform.sprint.dto.response.SprintResponse;

import java.util.List;
import java.util.UUID;

public interface SprintService {

    SprintResponse createSprint(CreateSprintRequest request);

    SprintResponse startSprint(UUID sprintId, StartSprintRequest request);

    SprintResponse completeSprint(UUID sprintId, CompleteSprintRequest request);

    SprintResponse getActiveSprint(UUID projectId);

    List<SprintResponse> getSprintsByProject(UUID projectId, SprintStatus status);

    SprintResponse getSprintById(UUID sprintId);

    SprintResponse updateSprint(UUID sprintId, UpdateSprintRequest request);

    void deleteSprint(UUID sprintId);
}
