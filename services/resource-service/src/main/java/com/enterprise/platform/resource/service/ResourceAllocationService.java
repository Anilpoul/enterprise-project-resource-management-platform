package com.enterprise.platform.resource.service;

import com.enterprise.platform.resource.dto.request.AllocateResourceRequest;
import com.enterprise.platform.resource.dto.request.UpdateAllocationRequest;
import com.enterprise.platform.resource.dto.response.ResourceAllocationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ResourceAllocationService {

    ResourceAllocationResponse allocateResource(AllocateResourceRequest request);

    ResourceAllocationResponse updateAllocation(UUID id, UpdateAllocationRequest request);

    ResourceAllocationResponse getAllocationById(UUID id);

    List<ResourceAllocationResponse> getAllocationsByResourceId(UUID resourceId);

    List<ResourceAllocationResponse> getAllocationsByProjectId(UUID projectId);

    void deallocateResource(UUID id);

    BigDecimal getCumulativeAllocation(UUID resourceId);
}
