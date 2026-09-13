package com.enterprise.platform.sprint.service.impl;

import com.enterprise.platform.events.SprintEvent;
import com.enterprise.platform.events.SprintEventType;
import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import com.enterprise.platform.sprint.context.TenantContext;
import com.enterprise.platform.sprint.dto.request.CompleteSprintRequest;
import com.enterprise.platform.sprint.dto.request.CreateSprintRequest;
import com.enterprise.platform.sprint.dto.request.StartSprintRequest;
import com.enterprise.platform.sprint.dto.request.UpdateSprintRequest;
import com.enterprise.platform.sprint.dto.response.SprintResponse;
import com.enterprise.platform.sprint.entity.Sprint;
import com.enterprise.platform.sprint.exception.BadRequestException;
import com.enterprise.platform.sprint.exception.ConflictException;
import com.enterprise.platform.sprint.exception.ResourceNotFoundException;
import com.enterprise.platform.sprint.kafka.producer.SprintEventProducer;
import com.enterprise.platform.sprint.mapper.SprintMapper;
import com.enterprise.platform.sprint.repository.SprintRepository;
import com.enterprise.platform.sprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final SprintEventProducer eventProducer;

    @Override
    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Creating sprint '{}' for project: {} in organization: {}", request.getName(), request.getProjectId(), organizationId);

        Sprint sprint = Sprint.builder()
                .organizationId(organizationId)
                .projectId(request.getProjectId())
                .name(request.getName().trim())
                .goal(request.getGoal())
                .status(SprintStatus.FUTURE)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalStoryPoints(0)
                .completedStoryPoints(0)
                .build();

        Sprint saved = sprintRepository.save(sprint);
        log.info("Sprint created with ID: {}", saved.getId());

        publishEvent(
                SprintEventType.SPRINT_CREATED,
                organizationId,
                saved.getProjectId(),
                saved.getId(),
                saved.getName(),
                saved.getStatus().name(),
                saved.getStartDate(),
                saved.getEndDate(),
                "Sprint created in planning phase"
        );

        return sprintMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SprintResponse startSprint(UUID sprintId, StartSprintRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Starting sprint ID: {} in organization: {}", sprintId, organizationId);

        Sprint sprint = findSprintOrThrow(sprintId, organizationId);

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BadRequestException("Sprint is already active");
        }
        if (sprint.getStatus() == SprintStatus.CLOSED) {
            throw new BadRequestException("Cannot start a closed sprint");
        }

        if (sprintRepository.existsByProjectIdAndStatus(sprint.getProjectId(), SprintStatus.ACTIVE)) {
            throw new ConflictException("An active sprint already exists for this project. Complete it before starting a new sprint.");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());

        Sprint started = sprintRepository.save(sprint);
        log.info("Sprint ID: {} is now ACTIVE", sprintId);

        publishEvent(
                SprintEventType.SPRINT_STARTED,
                organizationId,
                started.getProjectId(),
                started.getId(),
                started.getName(),
                started.getStatus().name(),
                started.getStartDate(),
                started.getEndDate(),
                "Sprint started"
        );

        return sprintMapper.toResponse(started);
    }

    @Override
    @Transactional
    public SprintResponse completeSprint(UUID sprintId, CompleteSprintRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Completing sprint ID: {} in organization: {}", sprintId, organizationId);

        Sprint sprint = findSprintOrThrow(sprintId, organizationId);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Only an ACTIVE sprint can be completed");
        }

        sprint.setStatus(SprintStatus.CLOSED);
        sprint.setCompletedAt(LocalDateTime.now());
        if (request != null) {
            if (request.getTotalStoryPoints() != null) {
                sprint.setTotalStoryPoints(request.getTotalStoryPoints());
            }
            if (request.getCompletedStoryPoints() != null) {
                sprint.setCompletedStoryPoints(request.getCompletedStoryPoints());
            }
        }

        Sprint completed = sprintRepository.save(sprint);
        log.info("Sprint ID: {} is now CLOSED", sprintId);

        publishEvent(
                SprintEventType.SPRINT_COMPLETED,
                organizationId,
                completed.getProjectId(),
                completed.getId(),
                completed.getName(),
                completed.getStatus().name(),
                completed.getStartDate(),
                completed.getEndDate(),
                "Sprint completed with " + completed.getCompletedStoryPoints() + "/" + completed.getTotalStoryPoints() + " story points"
        );

        return sprintMapper.toResponse(completed);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getActiveSprint(UUID projectId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching active sprint for project: {}", projectId);

        Sprint sprint = sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active sprint found for project: " + projectId));

        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(UUID projectId, SprintStatus status) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching sprints for project: {}, status filter: {}", projectId, status);

        List<Sprint> sprints;
        if (status != null) {
            sprints = sprintRepository.findByOrganizationIdAndProjectIdAndStatus(organizationId, projectId, status);
        } else {
            sprints = sprintRepository.findByOrganizationIdAndProjectIdOrderByCreatedAtDesc(organizationId, projectId);
        }

        return sprints.stream()
                .map(sprintMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getSprintById(UUID sprintId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching sprint by ID: {}", sprintId);

        Sprint sprint = findSprintOrThrow(sprintId, organizationId);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional
    public SprintResponse updateSprint(UUID sprintId, UpdateSprintRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating sprint ID: {}", sprintId);

        Sprint sprint = findSprintOrThrow(sprintId, organizationId);

        if (request.getName() != null && !request.getName().isBlank()) {
            sprint.setName(request.getName().trim());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        Sprint updated = sprintRepository.save(sprint);
        log.info("Sprint ID: {} updated successfully", sprintId);

        publishEvent(
                SprintEventType.SPRINT_UPDATED,
                organizationId,
                updated.getProjectId(),
                updated.getId(),
                updated.getName(),
                updated.getStatus().name(),
                updated.getStartDate(),
                updated.getEndDate(),
                "Sprint details updated"
        );

        return sprintMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSprint(UUID sprintId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting sprint ID: {}", sprintId);

        Sprint sprint = findSprintOrThrow(sprintId, organizationId);
        sprintRepository.delete(sprint);

        publishEvent(
                SprintEventType.SPRINT_DELETED,
                organizationId,
                sprint.getProjectId(),
                sprint.getId(),
                sprint.getName(),
                sprint.getStatus().name(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                "Sprint deleted"
        );
    }

    private Sprint findSprintOrThrow(UUID sprintId, UUID organizationId) {
        return sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));
    }

    private UUID getRequiredOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }

    private void publishEvent(
            SprintEventType eventType,
            UUID organizationId,
            UUID projectId,
            UUID sprintId,
            String sprintName,
            String status,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String details
    ) {
        SprintEvent event = SprintEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .organizationId(organizationId)
                .projectId(projectId)
                .sprintId(sprintId)
                .sprintName(sprintName)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        eventProducer.publish(event);
    }
}
