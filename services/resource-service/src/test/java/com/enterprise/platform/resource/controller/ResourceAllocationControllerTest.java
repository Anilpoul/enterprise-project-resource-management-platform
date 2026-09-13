package com.enterprise.platform.resource.controller;

import com.enterprise.platform.resource.dto.request.AllocateResourceRequest;
import com.enterprise.platform.resource.dto.request.UpdateAllocationRequest;
import com.enterprise.platform.resource.dto.response.ResourceAllocationResponse;
import com.enterprise.platform.resource.enums.AllocationStatus;
import com.enterprise.platform.resource.exception.GlobalExceptionHandler;
import com.enterprise.platform.resource.exception.ResourceNotFoundException;
import com.enterprise.platform.resource.service.ResourceAllocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResourceAllocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResourceAllocationService allocationService;

    @InjectMocks
    private ResourceAllocationController allocationController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID allocationId;
    private UUID resourceId;
    private UUID projectId;
    private ResourceAllocationResponse allocationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(allocationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        allocationId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        allocationResponse = ResourceAllocationResponse.builder()
                .id(allocationId)
                .resourceId(resourceId)
                .organizationId(UUID.randomUUID())
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .allocatedHoursPerWeek(new BigDecimal("20.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .notes("Core architecture lead")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/resources/allocations - Should allocate resource")
    void testAllocateResource() throws Exception {
        AllocateResourceRequest request = AllocateResourceRequest.builder()
                .resourceId(resourceId)
                .projectId(projectId)
                .allocationPercentage(new BigDecimal("50.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(AllocationStatus.ACTIVE)
                .notes("Core architecture lead")
                .build();

        when(allocationService.allocateResource(any(AllocateResourceRequest.class))).thenReturn(allocationResponse);

        mockMvc.perform(post("/api/v1/resources/allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(allocationId.toString()))
                .andExpect(jsonPath("$.data.allocationPercentage").value(50.00));
    }

    @Test
    @DisplayName("GET /api/v1/resources/allocations/{id} - Should return allocation details")
    void testGetAllocationById() throws Exception {
        when(allocationService.getAllocationById(allocationId)).thenReturn(allocationResponse);

        mockMvc.perform(get("/api/v1/resources/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(allocationId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/resources/allocations/{id} - Should return 404 when not found")
    void testGetAllocationById_NotFound() throws Exception {
        when(allocationService.getAllocationById(allocationId))
                .thenThrow(new ResourceNotFoundException("Resource allocation not found with ID: " + allocationId));

        mockMvc.perform(get("/api/v1/resources/allocations/{id}", allocationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resource allocation not found with ID: " + allocationId));
    }

    @Test
    @DisplayName("GET /api/v1/resources/{resourceId}/allocations - Should return allocations for resource")
    void testGetAllocationsByResourceId() throws Exception {
        when(allocationService.getAllocationsByResourceId(resourceId)).thenReturn(List.of(allocationResponse));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/allocations", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(allocationId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/allocations - Should return allocations for project")
    void testGetAllocationsByProjectId() throws Exception {
        when(allocationService.getAllocationsByProjectId(projectId)).thenReturn(List.of(allocationResponse));

        mockMvc.perform(get("/api/v1/projects/{projectId}/allocations", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].projectId").value(projectId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/resources/{resourceId}/cumulative-allocation - Should return cumulative percentage")
    void testGetCumulativeAllocation() throws Exception {
        when(allocationService.getCumulativeAllocation(resourceId)).thenReturn(new BigDecimal("75.00"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}/cumulative-allocation", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(75.00));
    }

    @Test
    @DisplayName("PUT /api/v1/resources/allocations/{id} - Should update allocation")
    void testUpdateAllocation() throws Exception {
        UpdateAllocationRequest request = UpdateAllocationRequest.builder()
                .allocationPercentage(new BigDecimal("75.00"))
                .build();

        when(allocationService.updateAllocation(eq(allocationId), any(UpdateAllocationRequest.class)))
                .thenReturn(allocationResponse);

        mockMvc.perform(put("/api/v1/resources/allocations/{id}", allocationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/resources/allocations/{id} - Should deallocate resource")
    void testDeallocateResource() throws Exception {
        doNothing().when(allocationService).deallocateResource(allocationId);

        mockMvc.perform(delete("/api/v1/resources/allocations/{id}", allocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource deallocated successfully"));
    }
}
