package com.enterprise.platform.resource.service.impl;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.events.ResourceEventType;
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
import com.enterprise.platform.resource.service.ResourceAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    private final ResourceAllocationRepository allocationRepository;
    private final ResourceProfileRepository profileRepository;
    private final ResourceAllocationMapper allocationMapper;
    private final ResourceEventProducer eventProducer;

    private static final List<AllocationStatus> ACTIVE_STATUSES = List.of(AllocationStatus.ACTIVE, AllocationStatus.PLANNED);

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }

    @Override
    @Transactional
    public ResourceAllocationResponse allocateResource(AllocateResourceRequest request) {
        UUID orgId = getRequiredOrgId();
        log.info("Allocating resource: {} to project: {} in org: {}", request.getResourceId(), request.getProjectId(), orgId);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        ResourceProfile profile = profileRepository.findByIdAndOrganizationId(request.getResourceId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource profile not found with ID: " + request.getResourceId()));

        if (profile.getStatus() == ResourceStatus.INACTIVE) {
            throw new BadRequestException("Cannot allocate an inactive resource");
        }

        BigDecimal capacity = profile.getWeeklyCapacityHours() != null ? profile.getWeeklyCapacityHours() : new BigDecimal("40.00");
        BigDecimal allocatedHours = capacity.multiply(request.getAllocationPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        AllocationStatus status = request.getStatus();
        if (status == null) {
            status = request.getStartDate().isAfter(LocalDate.now()) ? AllocationStatus.PLANNED : AllocationStatus.ACTIVE;
        }

        ResourceAllocation allocation = allocationMapper.toEntity(request);
        allocation.setResourceProfile(profile);
        allocation.setOrganizationId(orgId);
        allocation.setAllocatedHoursPerWeek(allocatedHours);
        allocation.setStatus(status);

        ResourceAllocation savedAllocation = allocationRepository.save(allocation);

        if (profile.getStatus() == ResourceStatus.AVAILABLE) {
            profile.setStatus(ResourceStatus.ALLOCATED);
            profileRepository.save(profile);
        }

        BigDecimal cumulativePercentage = allocationRepository.sumAllocationPercentageByResource(
                profile.getId(), orgId, ACTIVE_STATUSES, null);
        if (cumulativePercentage != null && cumulativePercentage.compareTo(new BigDecimal("100.00")) > 0) {
            log.warn("Over-allocation detected: Resource {} has cumulative allocation of {}%", profile.getId(), cumulativePercentage);
        }

        eventProducer.publish(ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_ALLOCATED)
                .organizationId(orgId)
                .resourceId(profile.getId())
                .userId(profile.getUserId())
                .projectId(savedAllocation.getProjectId())
                .allocationId(savedAllocation.getId())
                .allocationPercentage(savedAllocation.getAllocationPercentage() != null ? savedAllocation.getAllocationPercentage().intValue() : null)
                .startDate(savedAllocation.getStartDate())
                .endDate(savedAllocation.getEndDate())
                .timestamp(LocalDateTime.now())
                .details(String.format("Allocated %s%% to project %s", savedAllocation.getAllocationPercentage(), savedAllocation.getProjectId()))
                .build());

        return allocationMapper.toResponse(savedAllocation);
    }

    @Override
    @Transactional
    public ResourceAllocationResponse updateAllocation(UUID id, UpdateAllocationRequest request) {
        UUID orgId = getRequiredOrgId();
        ResourceAllocation allocation = allocationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource allocation not found with ID: " + id));

        LocalDate start = request.getStartDate() != null ? request.getStartDate() : allocation.getStartDate();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : allocation.getEndDate();
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (request.getAllocationPercentage() != null) {
            BigDecimal capacity = allocation.getResourceProfile().getWeeklyCapacityHours() != null
                    ? allocation.getResourceProfile().getWeeklyCapacityHours()
                    : new BigDecimal("40.00");
            BigDecimal allocatedHours = capacity.multiply(request.getAllocationPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            allocation.setAllocatedHoursPerWeek(allocatedHours);
        }

        allocationMapper.updateEntityFromDto(request, allocation);
        ResourceAllocation updated = allocationRepository.save(allocation);

        eventProducer.publish(ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.ALLOCATION_UPDATED)
                .organizationId(orgId)
                .resourceId(updated.getResourceProfile().getId())
                .userId(updated.getResourceProfile().getUserId())
                .projectId(updated.getProjectId())
                .allocationId(updated.getId())
                .allocationPercentage(updated.getAllocationPercentage() != null ? updated.getAllocationPercentage().intValue() : null)
                .startDate(updated.getStartDate())
                .endDate(updated.getEndDate())
                .timestamp(LocalDateTime.now())
                .details("Allocation updated")
                .build());

        return allocationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceAllocationResponse getAllocationById(UUID id) {
        UUID orgId = getRequiredOrgId();
        ResourceAllocation allocation = allocationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource allocation not found with ID: " + id));

        return allocationMapper.toResponse(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceAllocationResponse> getAllocationsByResourceId(UUID resourceId) {
        UUID orgId = getRequiredOrgId();
        List<ResourceAllocation> allocations = allocationRepository.findByResourceProfileIdAndOrganizationId(resourceId, orgId);
        return allocations.stream()
                .map(allocationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceAllocationResponse> getAllocationsByProjectId(UUID projectId) {
        UUID orgId = getRequiredOrgId();
        List<ResourceAllocation> allocations = allocationRepository.findByProjectIdAndOrganizationId(projectId, orgId);
        return allocations.stream()
                .map(allocationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deallocateResource(UUID id) {
        UUID orgId = getRequiredOrgId();
        ResourceAllocation allocation = allocationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource allocation not found with ID: " + id));

        ResourceProfile profile = allocation.getResourceProfile();
        UUID projectId = allocation.getProjectId();
        BigDecimal percentage = allocation.getAllocationPercentage();

        allocationRepository.delete(allocation);
        log.info("Deleted resource allocation: {} for org: {}", id, orgId);

        List<ResourceAllocation> remaining = allocationRepository.findByResourceProfileIdAndOrganizationId(profile.getId(), orgId);
        boolean hasActive = remaining.stream().anyMatch(a -> ACTIVE_STATUSES.contains(a.getStatus()) && !a.getId().equals(id));
        if (!hasActive && profile.getStatus() == ResourceStatus.ALLOCATED) {
            profile.setStatus(ResourceStatus.AVAILABLE);
            profileRepository.save(profile);
        }

        eventProducer.publish(ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_DEALLOCATED)
                .organizationId(orgId)
                .resourceId(profile.getId())
                .userId(profile.getUserId())
                .projectId(projectId)
                .allocationId(id)
                .allocationPercentage(percentage != null ? percentage.intValue() : null)
                .timestamp(LocalDateTime.now())
                .details("Deallocated from project " + projectId)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCumulativeAllocation(UUID resourceId) {
        UUID orgId = getRequiredOrgId();
        BigDecimal sum = allocationRepository.sumAllocationPercentageByResource(resourceId, orgId, ACTIVE_STATUSES, null);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
