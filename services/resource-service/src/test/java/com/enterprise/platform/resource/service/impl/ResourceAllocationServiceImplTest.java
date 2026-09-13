package com.enterprise.platform.resource.service.impl;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.resource.context.TenantContext;
import com.enterprise.platform.resource.dto.request.AllocateResourceRequest;
import com.enterprise.platform.resource.dto.request.UpdateAllocationRequest;
import com.enterprise.platform.resource.dto.response.ResourceAllocationResponse;
import com.enterprise.platform.resource.entity.ResourceAllocation;
import com.enterprise.platform.resource.entity.ResourceProfile;
import com.enterprise.platform.resource.enums.AllocationStatus;
import com.enterprise.platform.resource.enums.ResourceStatus;
import com.enterprise.platform.resource.exception.BadRequestException;
import com.enterprise.platform.resource.exception.ResourceNotFoundException;
import com.enterprise.platform.resource.kafka.producer.ResourceEventProducer;
import com.enterprise.platform.resource.mapper.ResourceAllocationMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceAllocationServiceImplTest {

    @Mock
    private ResourceAllocationRepository allocationRepository;

    @Mock
    private ResourceProfileRepository profileRepository;

    @Mock
    private ResourceAllocationMapper allocationMapper;

    @Mock
    private ResourceEventProducer eventProducer;

    @InjectMocks
    private ResourceAllocationServiceImpl allocationService;

    private UUID organizationId;
    private UUID resourceId;
    private UUID projectId;
    private UUID allocationId;
    private ResourceProfile profile;
    private ResourceAllocation allocation;
    private ResourceAllocationResponse allocationResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        allocationId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        profile = ResourceProfile.builder()
                .id(resourceId)
                .organizationId(organizationId)
                .userId(UUID.randomUUID())
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .status(ResourceStatus.AVAILABLE)
                .build();

        allocation = ResourceAllocation.builder()
                .id(allocationId)
                .resourceProfile(profile)
                .organizationId(organizationId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .allocatedHoursPerWeek(new BigDecimal("20.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .notes("Core architecture lead")
                .build();

        allocationResponse = ResourceAllocationResponse.builder()
                .id(allocationId)
                .resourceId(resourceId)
                .organizationId(organizationId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .allocatedHoursPerWeek(new BigDecimal("20.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .notes("Core architecture lead")
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should allocate resource successfully and change profile status to ALLOCATED")
    void testAllocateResource_Success() {
        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .notes("Core architecture lead")
                .build();

        when(profileRepository.findByIdAndOrganizationId(resourceId, organizationId)).thenReturn(Optional.of(profile));
        when(allocationMapper.toEntity(request)).thenReturn(allocation);
        when(allocationRepository.save(any(ResourceAllocation.class))).thenReturn(allocation);
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(resourceId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("50.00"));

        ResourceAllocationResponse result = allocationService.allocateResource(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(allocationId);
        assertThat(result.getAllocationPercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getAllocatedHoursPerWeek()).isEqualByComparingTo(new BigDecimal("20.00"));

        assertThat(profile.getStatus()).isEqualTo(ResourceStatus.ALLOCATED);
        verify(profileRepository).save(profile);
        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should detect over-allocation when cumulative percentage exceeds 100%")
    void testAllocateResource_OverAllocationDetection() {
        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("70.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .build();

        when(profileRepository.findByIdAndOrganizationId(resourceId, organizationId)).thenReturn(Optional.of(profile));
        when(allocationMapper.toEntity(request)).thenReturn(allocation);
        when(allocationRepository.save(any(ResourceAllocation.class))).thenReturn(allocation);
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);
        when(allocationRepository.sumAllocationPercentageByResource(eq(resourceId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("120.00"));

        ResourceAllocationResponse result = allocationService.allocateResource(request);

        assertThat(result).isNotNull();
        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when start date is after end date")
    void testAllocateResource_InvalidDates_ThrowsBadRequest() {
        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now())
                .build();

        assertThatThrownBy(() -> allocationService.allocateResource(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile not found")
    void testAllocateResource_ProfileNotFound_ThrowsNotFound() {
        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        when(profileRepository.findByIdAndOrganizationId(resourceId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.allocateResource(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource profile not found with ID");
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to allocate inactive resource")
    void testAllocateResource_InactiveProfile_ThrowsBadRequest() {
        profile.setStatus(ResourceStatus.INACTIVE);

        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        when(profileRepository.findByIdAndOrganizationId(resourceId, organizationId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> allocationService.allocateResource(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot allocate an inactive resource");
    }

    @Test
    @DisplayName("Should update allocation successfully and recalculate hours")
    void testUpdateAllocation_Success() {
        UpdateAllocationRequest request = UpdateAllocationRequest.builder()
                .allocationPercentage(new BigDecimal("75.00"))
                .build();

        when(allocationRepository.findByIdAndOrganizationId(allocationId, organizationId)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(allocation)).thenReturn(allocation);
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);

        ResourceAllocationResponse result = allocationService.updateAllocation(allocationId, request);

        assertThat(result).isNotNull();
        verify(allocationMapper).updateEntityFromDto(request, allocation);
        assertThat(allocation.getAllocatedHoursPerWeek()).isEqualByComparingTo(new BigDecimal("30.00"));
        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException on update when start date is after end date")
    void testUpdateAllocation_InvalidDates_ThrowsBadRequest() {
        UpdateAllocationRequest request = UpdateAllocationRequest.builder()
                .startDate(LocalDate.now().plusDays(20))
                .endDate(LocalDate.now())
                .build();

        when(allocationRepository.findByIdAndOrganizationId(allocationId, organizationId)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> allocationService.updateAllocation(allocationId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    @DisplayName("Should get allocation by ID successfully")
    void testGetAllocationById_Success() {
        when(allocationRepository.findByIdAndOrganizationId(allocationId, organizationId)).thenReturn(Optional.of(allocation));
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);

        ResourceAllocationResponse result = allocationService.getAllocationById(allocationId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(allocationId);
    }

    @Test
    @DisplayName("Should get allocations by resource ID")
    void testGetAllocationsByResourceId_Success() {
        when(allocationRepository.findByResourceProfileIdAndOrganizationId(resourceId, organizationId))
                .thenReturn(List.of(allocation));
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);

        List<ResourceAllocationResponse> result = allocationService.getAllocationsByResourceId(resourceId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should get allocations by project ID")
    void testGetAllocationsByProjectId_Success() {
        when(allocationRepository.findByProjectIdAndOrganizationId(projectId, organizationId))
                .thenReturn(List.of(allocation));
        when(allocationMapper.toResponse(allocation)).thenReturn(allocationResponse);

        List<ResourceAllocationResponse> result = allocationService.getAllocationsByProjectId(projectId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should deallocate resource and revert profile status to AVAILABLE when no other allocations exist")
    void testDeallocateResource_RevertsProfileStatusToAvailable() {
        profile.setStatus(ResourceStatus.ALLOCATED);

        when(allocationRepository.findByIdAndOrganizationId(allocationId, organizationId)).thenReturn(Optional.of(allocation));
        when(allocationRepository.findByResourceProfileIdAndOrganizationId(resourceId, organizationId))
                .thenReturn(Collections.emptyList());

        allocationService.deallocateResource(allocationId);

        verify(allocationRepository).delete(allocation);
        assertThat(profile.getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
        verify(profileRepository).save(profile);
        verify(eventProducer).publish(any(ResourceEvent.class));
    }

    @Test
    @DisplayName("Should get cumulative allocation percentage")
    void testGetCumulativeAllocation_Success() {
        when(allocationRepository.sumAllocationPercentageByResource(eq(resourceId), eq(organizationId), any(), any()))
                .thenReturn(new BigDecimal("80.00"));

        BigDecimal result = allocationService.getCumulativeAllocation(resourceId);

        assertThat(result).isEqualByComparingTo(new BigDecimal("80.00"));
    }
}
