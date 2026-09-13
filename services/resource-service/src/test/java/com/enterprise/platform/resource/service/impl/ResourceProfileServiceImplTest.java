package com.enterprise.platform.resource.service.impl;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.resource.context.TenantContext;
import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.PagedResponse;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.dto.response.WorkloadReportResponse;
import com.enterprise.platform.resource.entity.ResourceAllocation;
import com.enterprise.platform.resource.entity.ResourceProfile;
import com.enterprise.platform.resource.enums.AllocationStatus;
import com.enterprise.platform.resource.enums.ResourceStatus;
import com.enterprise.platform.resource.exception.BadRequestException;
import com.enterprise.platform.resource.exception.ConflictException;
import com.enterprise.platform.resource.exception.ResourceNotFoundException;
import com.enterprise.platform.resource.kafka.producer.ResourceEventProducer;
import com.enterprise.platform.resource.mapper.ResourceProfileMapper;
import com.enterprise.platform.resource.repository.ResourceAllocationRepository;
import com.enterprise.platform.resource.repository.ResourceProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceProfileServiceImplTest {

    @Mock
    private ResourceProfileRepository profileRepository;

    @Mock
    private ResourceAllocationRepository allocationRepository;

    @Mock
    private ResourceProfileMapper profileMapper;

    @Mock
    private ResourceEventProducer eventProducer;

    @InjectMocks
    private ResourceProfileServiceImpl profileService;

    private UUID organizationId;
    private UUID userId;
    private UUID profileId;
    private ResourceProfile profile;
    private ResourceProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        profile = ResourceProfile.builder()
                .id(profileId)
                .organizationId(organizationId)
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .skills("Java, Spring Boot, Kafka, Docker")
                .hourlyRate(new BigDecimal("120.00"))
                .currency("USD")
                .status(ResourceStatus.AVAILABLE)
                .build();

        profileResponse = ResourceProfileResponse.builder()
                .id(profileId)
                .organizationId(organizationId)
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .skills("Java, Spring Boot, Kafka, Docker")
                .hourlyRate(new BigDecimal("120.00"))
                .currency("USD")
                .status(ResourceStatus.AVAILABLE)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create resource profile successfully")
    void testCreateProfile_Success() {
        CreateResourceProfileRequest request = CreateResourceProfileRequest.builder()
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .skills("Java, Spring Boot, Kafka, Docker")
                .hourlyRate(new BigDecimal("120.00"))
                .currency("USD")
                .status(ResourceStatus.AVAILABLE)
                .build();

        when(profileRepository.existsByOrganizationIdAndUserId(organizationId, userId)).thenReturn(false);
        when(profileMapper.toEntity(request)).thenReturn(profile);
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        ResourceProfileResponse result = profileService.createProfile(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(profileId);
        assertThat(result.getWeeklyCapacityHours()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.getRemainingCapacityHours()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.getIsOverAllocated()).isFalse();

        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when TenantContext is missing")
    void testCreateProfile_MissingTenant_ThrowsBadRequest() {
        TenantContext.clear();

        CreateResourceProfileRequest request = CreateResourceProfileRequest.builder()
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .build();

        assertThatThrownBy(() -> profileService.createProfile(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }

    @Test
    @DisplayName("Should throw ConflictException when profile already exists for user")
    void testCreateProfile_DuplicateUser_ThrowsConflict() {
        CreateResourceProfileRequest request = CreateResourceProfileRequest.builder()
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .build();

        when(profileRepository.existsByOrganizationIdAndUserId(organizationId, userId)).thenReturn(true);

        assertThatThrownBy(() -> profileService.createProfile(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists for user ID");
    }

    @Test
    @DisplayName("Should get profile by ID successfully")
    void testGetProfileById_Success() {
        when(profileRepository.findByIdAndOrganizationId(profileId, organizationId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("50.00"));

        ResourceProfileResponse result = profileService.getProfileById(profileId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAllocatedPercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getAllocatedHoursPerWeek()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getRemainingCapacityHours()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getIsOverAllocated()).isFalse();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile not found by ID")
    void testGetProfileById_NotFound_ThrowsException() {
        when(profileRepository.findByIdAndOrganizationId(profileId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileById(profileId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource profile not found with ID");
    }

    @Test
    @DisplayName("Should get profile by user ID successfully")
    void testGetProfileByUserId_Success() {
        when(profileRepository.findByOrganizationIdAndUserId(organizationId, userId)).thenReturn(Optional.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        ResourceProfileResponse result = profileService.getProfileByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should get profiles with status filter")
    void testGetProfiles_WithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ResourceProfile> page = new PageImpl<>(List.of(profile), pageable, 1);

        when(profileRepository.findByOrganizationIdAndStatus(organizationId, ResourceStatus.AVAILABLE, pageable))
                .thenReturn(page);
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        PagedResponse<ResourceProfileResponse> result = profileService.getProfiles(ResourceStatus.AVAILABLE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get profiles without status filter")
    void testGetProfiles_WithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ResourceProfile> page = new PageImpl<>(List.of(profile), pageable, 1);

        when(profileRepository.findByOrganizationId(organizationId, pageable)).thenReturn(page);
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        PagedResponse<ResourceProfileResponse> result = profileService.getProfiles(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should search profiles by skill")
    void testSearchBySkill_Success() {
        when(profileRepository.findByOrganizationIdAndSkillsContainingIgnoreCase(organizationId, "Java"))
                .thenReturn(List.of(profile));
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        List<ResourceProfileResponse> result = profileService.searchBySkill("Java");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkills()).contains("Java");
    }

    @Test
    @DisplayName("Should update resource profile successfully")
    void testUpdateProfile_Success() {
        UpdateResourceProfileRequest request = UpdateResourceProfileRequest.builder()
                .jobTitle("Staff Engineer")
                .hourlyRate(new BigDecimal("140.00"))
                .build();

        when(profileRepository.findByIdAndOrganizationId(profileId, organizationId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toResponse(profile)).thenReturn(profileResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        ResourceProfileResponse result = profileService.updateProfile(profileId, request);

        assertThat(result).isNotNull();
        verify(profileMapper).updateEntityFromDto(request, profile);
        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should delete resource profile successfully")
    void testDeleteProfile_Success() {
        when(profileRepository.findByIdAndOrganizationId(profileId, organizationId)).thenReturn(Optional.of(profile));

        profileService.deleteProfile(profileId);

        verify(profileRepository).delete(profile);
    }

    @Test
    @DisplayName("Should generate workload report successfully")
    void testGetWorkloadReport_Success() {
        when(profileRepository.findByOrganizationId(organizationId)).thenReturn(List.of(profile));
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("80.00"));
        when(allocationRepository.findByResourceProfileIdAndOrganizationId(profileId, organizationId))
                .thenReturn(List.of(ResourceAllocation.builder().id(UUID.randomUUID()).status(AllocationStatus.ACTIVE).build()));

        WorkloadReportResponse report = profileService.getWorkloadReport();

        assertThat(report).isNotNull();
        assertThat(report.getTotalResources()).isEqualTo(1);
        assertThat(report.getTotalAllocatedResources()).isEqualTo(1);
        assertThat(report.getOverAllocatedResources()).isEqualTo(0);
        assertThat(report.getAverageUtilizationPercentage()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(report.getResources().get(0).isOverAllocated()).isFalse();
    }

    @Test
    @DisplayName("Should detect over-allocated resources in workload report")
    void testGetWorkloadReport_OverAllocated() {
        when(profileRepository.findByOrganizationId(organizationId)).thenReturn(List.of(profile));
        when(allocationRepository.sumAllocationPercentageByResource(eq(profileId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("120.00"));
        when(allocationRepository.findByResourceProfileIdAndOrganizationId(profileId, organizationId))
                .thenReturn(List.of(
                        ResourceAllocation.builder().id(UUID.randomUUID()).status(AllocationStatus.ACTIVE).build(),
                        ResourceAllocation.builder().id(UUID.randomUUID()).status(AllocationStatus.ACTIVE).build()
                ));

        WorkloadReportResponse report = profileService.getWorkloadReport();

        assertThat(report).isNotNull();
        assertThat(report.getTotalResources()).isEqualTo(1);
        assertThat(report.getTotalAllocatedResources()).isEqualTo(1);
        assertThat(report.getOverAllocatedResources()).isEqualTo(1);
        assertThat(report.getAverageUtilizationPercentage()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(report.getResources().get(0).isOverAllocated()).isTrue();
    }
}
