package com.enterprise.platform.resource.service.impl;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.events.ResourceEventType;
import com.enterprise.platform.resource.context.TenantContext;
import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.PagedResponse;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.dto.response.ResourceUtilizationDto;
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
import com.enterprise.platform.resource.service.ResourceProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceProfileServiceImpl implements ResourceProfileService {

    private final ResourceProfileRepository profileRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceProfileMapper profileMapper;
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
    public ResourceProfileResponse createProfile(CreateResourceProfileRequest request) {
        UUID orgId = getRequiredOrgId();
        log.info("Creating resource profile for user: {} in org: {}", request.getUserId(), orgId);

        if (profileRepository.existsByOrganizationIdAndUserId(orgId, request.getUserId())) {
            throw new ConflictException("Resource profile already exists for user ID: " + request.getUserId());
        }

        ResourceProfile profile = profileMapper.toEntity(request);
        profile.setOrganizationId(orgId);
        if (profile.getWeeklyCapacityHours() == null) {
            profile.setWeeklyCapacityHours(new BigDecimal("40.00"));
        }
        if (profile.getCurrency() == null) {
            profile.setCurrency("USD");
        }
        if (profile.getStatus() == null) {
            profile.setStatus(ResourceStatus.AVAILABLE);
        }

        ResourceProfile savedProfile = profileRepository.save(profile);

        eventProducer.publish(ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_CREATED)
                .organizationId(orgId)
                .resourceId(savedProfile.getId())
                .userId(savedProfile.getUserId())
                .timestamp(LocalDateTime.now())
                .details("Resource profile created for user " + savedProfile.getUserId())
                .build());

        ResourceProfileResponse response = profileMapper.toResponse(savedProfile);
        enrichProfileResponse(savedProfile, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceProfileResponse getProfileById(UUID id) {
        UUID orgId = getRequiredOrgId();
        ResourceProfile profile = profileRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource profile not found with ID: " + id));

        ResourceProfileResponse response = profileMapper.toResponse(profile);
        enrichProfileResponse(profile, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceProfileResponse getProfileByUserId(UUID userId) {
        UUID orgId = getRequiredOrgId();
        ResourceProfile profile = profileRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource profile not found for user ID: " + userId));

        ResourceProfileResponse response = profileMapper.toResponse(profile);
        enrichProfileResponse(profile, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ResourceProfileResponse> getProfiles(ResourceStatus status, Pageable pageable) {
        UUID orgId = getRequiredOrgId();
        Page<ResourceProfile> page = (status != null)
                ? profileRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
                : profileRepository.findByOrganizationId(orgId, pageable);

        List<ResourceProfileResponse> content = page.getContent().stream()
                .map(profile -> {
                    ResourceProfileResponse response = profileMapper.toResponse(profile);
                    enrichProfileResponse(profile, response);
                    return response;
                })
                .toList();

        return PagedResponse.<ResourceProfileResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceProfileResponse> searchBySkill(String skill) {
        UUID orgId = getRequiredOrgId();
        List<ResourceProfile> profiles = profileRepository.findByOrganizationIdAndSkillsContainingIgnoreCase(orgId, skill);
        return profiles.stream()
                .map(profile -> {
                    ResourceProfileResponse response = profileMapper.toResponse(profile);
                    enrichProfileResponse(profile, response);
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public ResourceProfileResponse updateProfile(UUID id, UpdateResourceProfileRequest request) {
        UUID orgId = getRequiredOrgId();
        ResourceProfile profile = profileRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource profile not found with ID: " + id));

        profileMapper.updateEntityFromDto(request, profile);
        ResourceProfile updated = profileRepository.save(profile);

        eventProducer.publish(ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_UPDATED)
                .organizationId(orgId)
                .resourceId(updated.getId())
                .userId(updated.getUserId())
                .timestamp(LocalDateTime.now())
                .details("Resource profile updated for user " + updated.getUserId())
                .build());

        ResourceProfileResponse response = profileMapper.toResponse(updated);
        enrichProfileResponse(updated, response);
        return response;
    }

    @Override
    @Transactional
    public void deleteProfile(UUID id) {
        UUID orgId = getRequiredOrgId();
        ResourceProfile profile = profileRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource profile not found with ID: " + id));

        profileRepository.delete(profile);
        log.info("Deleted resource profile: {} for org: {}", id, orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkloadReportResponse getWorkloadReport() {
        UUID orgId = getRequiredOrgId();
        List<ResourceProfile> profiles = profileRepository.findByOrganizationId(orgId);

        int totalResources = profiles.size();
        int totalAllocated = 0;
        int overAllocated = 0;
        BigDecimal sumUtilization = BigDecimal.ZERO;

        List<ResourceUtilizationDto> resourceDtos = new java.util.ArrayList<>();

        for (ResourceProfile profile : profiles) {
            BigDecimal totalAllocatedPercentage = allocationRepository.sumAllocationPercentageByResource(
                    profile.getId(), orgId, ACTIVE_STATUSES, null);
            if (totalAllocatedPercentage == null) {
                totalAllocatedPercentage = BigDecimal.ZERO;
            }

            BigDecimal capacity = profile.getWeeklyCapacityHours() != null ? profile.getWeeklyCapacityHours() : new BigDecimal("40.00");
            BigDecimal allocatedHours = capacity.multiply(totalAllocatedPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal remainingHours = capacity.subtract(allocatedHours);
            if (remainingHours.compareTo(BigDecimal.ZERO) < 0) {
                remainingHours = BigDecimal.ZERO;
            }

            boolean isOver = totalAllocatedPercentage.compareTo(new BigDecimal("100.00")) > 0;
            if (totalAllocatedPercentage.compareTo(BigDecimal.ZERO) > 0) {
                totalAllocated++;
            }
            if (isOver) {
                overAllocated++;
            }
            sumUtilization = sumUtilization.add(totalAllocatedPercentage);

            List<ResourceAllocation> allocations = allocationRepository.findByResourceProfileIdAndOrganizationId(profile.getId(), orgId);
            long activeCount = allocations.stream().filter(a -> ACTIVE_STATUSES.contains(a.getStatus())).count();

            resourceDtos.add(ResourceUtilizationDto.builder()
                    .resourceId(profile.getId())
                    .userId(profile.getUserId())
                    .jobTitle(profile.getJobTitle())
                    .weeklyCapacityHours(capacity)
                    .totalAllocatedPercentage(totalAllocatedPercentage)
                    .allocatedHoursPerWeek(allocatedHours)
                    .remainingCapacityHours(remainingHours)
                    .status(profile.getStatus())
                    .isOverAllocated(isOver)
                    .activeAllocationsCount((int) activeCount)
                    .build());
        }

        BigDecimal averageUtilization = totalResources > 0
                ? sumUtilization.divide(new BigDecimal(totalResources), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return WorkloadReportResponse.builder()
                .organizationId(orgId)
                .totalResources(totalResources)
                .totalAllocatedResources(totalAllocated)
                .overAllocatedResources(overAllocated)
                .averageUtilizationPercentage(averageUtilization)
                .resources(resourceDtos)
                .build();
    }

    private void enrichProfileResponse(ResourceProfile profile, ResourceProfileResponse response) {
        BigDecimal totalAllocatedPercentage = allocationRepository.sumAllocationPercentageByResource(
                profile.getId(), profile.getOrganizationId(), ACTIVE_STATUSES, null);
        if (totalAllocatedPercentage == null) {
            totalAllocatedPercentage = BigDecimal.ZERO;
        }

        BigDecimal capacity = profile.getWeeklyCapacityHours() != null ? profile.getWeeklyCapacityHours() : new BigDecimal("40.00");
        BigDecimal allocatedHours = capacity.multiply(totalAllocatedPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal remainingHours = capacity.subtract(allocatedHours);
        if (remainingHours.compareTo(BigDecimal.ZERO) < 0) {
            remainingHours = BigDecimal.ZERO;
        }

        response.setTotalAllocatedPercentage(totalAllocatedPercentage);
        response.setAllocatedHoursPerWeek(allocatedHours);
        response.setRemainingCapacityHours(remainingHours);
        response.setIsOverAllocated(totalAllocatedPercentage.compareTo(new BigDecimal("100.00")) > 0);
    }
}
