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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private SprintEventProducer eventProducer;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private UUID organizationId;
    private UUID projectId;
    private UUID sprintId;
    private Sprint sprint;
    private SprintResponse sprintResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        sprintId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        sprint = Sprint.builder()
                .id(sprintId)
                .organizationId(organizationId)
                .projectId(projectId)
                .name("Sprint 1 - Foundation")
                .goal("Setup infra & core APIs")
                .status(SprintStatus.FUTURE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .totalStoryPoints(20)
                .completedStoryPoints(0)
                .build();

        sprintResponse = SprintResponse.builder()
                .id(sprintId)
                .organizationId(organizationId)
                .projectId(projectId)
                .name("Sprint 1 - Foundation")
                .goal("Setup infra & core APIs")
                .status(SprintStatus.FUTURE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .totalStoryPoints(20)
                .completedStoryPoints(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create sprint successfully and publish SPRINT_CREATED event")
    void testCreateSprint_Success() {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .projectId(projectId)
                .name("Sprint 1 - Foundation")
                .goal("Setup infra & core APIs")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();

        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse response = sprintService.createSprint(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Sprint 1 - Foundation");
        assertThat(response.getStatus()).isEqualTo(SprintStatus.FUTURE);

        verify(sprintRepository).save(any(Sprint.class));
        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        SprintEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(SprintEventType.SPRINT_CREATED);
        assertThat(event.getSprintName()).isEqualTo("Sprint 1 - Foundation");
    }

    @Test
    @DisplayName("Should throw BadRequestException when tenant header is missing")
    void testCreateSprint_MissingTenantContext() {
        TenantContext.clear();
        CreateSprintRequest request = CreateSprintRequest.builder()
                .projectId(projectId)
                .name("Sprint 1")
                .build();

        assertThatThrownBy(() -> sprintService.createSprint(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id");
    }

    @Test
    @DisplayName("Should start sprint successfully and publish SPRINT_STARTED event")
    void testStartSprint_Success() {
        StartSprintRequest request = StartSprintRequest.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();

        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)).thenReturn(false);
        when(sprintRepository.save(sprint)).thenReturn(sprint);

        SprintResponse activeResponse = SprintResponse.builder()
                .id(sprintId)
                .status(SprintStatus.ACTIVE)
                .build();
        when(sprintMapper.toResponse(sprint)).thenReturn(activeResponse);

        SprintResponse response = sprintService.startSprint(sprintId, request);

        assertThat(response).isNotNull();
        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        verify(sprintRepository).save(sprint);

        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(SprintEventType.SPRINT_STARTED);
    }

    @Test
    @DisplayName("Should throw ConflictException when another sprint is already active in project")
    void testStartSprint_ActiveSprintConflict() {
        StartSprintRequest request = StartSprintRequest.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();

        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> sprintService.startSprint(sprintId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("An active sprint already exists");

        verify(sprintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should complete active sprint successfully and publish SPRINT_COMPLETED event")
    void testCompleteSprint_Success() {
        sprint.setStatus(SprintStatus.ACTIVE);
        CompleteSprintRequest request = CompleteSprintRequest.builder()
                .totalStoryPoints(25)
                .completedStoryPoints(22)
                .build();

        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(sprint)).thenReturn(sprint);

        SprintResponse closedResponse = SprintResponse.builder()
                .id(sprintId)
                .status(SprintStatus.CLOSED)
                .build();
        when(sprintMapper.toResponse(sprint)).thenReturn(closedResponse);

        SprintResponse response = sprintService.completeSprint(sprintId, request);

        assertThat(response).isNotNull();
        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.CLOSED);
        assertThat(sprint.getCompletedStoryPoints()).isEqualTo(22);
        verify(sprintRepository).save(sprint);

        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(SprintEventType.SPRINT_COMPLETED);
    }

    @Test
    @DisplayName("Should throw BadRequestException when completing a non-active sprint")
    void testCompleteSprint_NotActive() {
        sprint.setStatus(SprintStatus.FUTURE);
        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.completeSprint(sprintId, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only an ACTIVE sprint can be completed");
    }

    @Test
    @DisplayName("Should get active sprint for project")
    void testGetActiveSprint_Success() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)).thenReturn(Optional.of(sprint));
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse response = sprintService.getActiveSprint(projectId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no active sprint for project")
    void testGetActiveSprint_NotFound() {
        when(sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.getActiveSprint(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No active sprint found");
    }

    @Test
    @DisplayName("Should get sprints by project with status filter")
    void testGetSprintsByProject_WithStatus() {
        when(sprintRepository.findByOrganizationIdAndProjectIdAndStatus(organizationId, projectId, SprintStatus.FUTURE))
                .thenReturn(List.of(sprint));
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        List<SprintResponse> list = sprintService.getSprintsByProject(projectId, SprintStatus.FUTURE);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Should update sprint and publish SPRINT_UPDATED event")
    void testUpdateSprint_Success() {
        UpdateSprintRequest request = UpdateSprintRequest.builder()
                .name("Sprint 1 - Updated")
                .goal("New goal")
                .build();

        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(sprint)).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse response = sprintService.updateSprint(sprintId, request);

        assertThat(response).isNotNull();
        assertThat(sprint.getName()).isEqualTo("Sprint 1 - Updated");
        verify(sprintRepository).save(sprint);

        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(SprintEventType.SPRINT_UPDATED);
    }

    @Test
    @DisplayName("Should delete sprint and publish SPRINT_DELETED event")
    void testDeleteSprint_Success() {
        when(sprintRepository.findByIdAndOrganizationId(sprintId, organizationId)).thenReturn(Optional.of(sprint));

        sprintService.deleteSprint(sprintId);

        verify(sprintRepository).delete(sprint);
        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(SprintEventType.SPRINT_DELETED);
    }
}
