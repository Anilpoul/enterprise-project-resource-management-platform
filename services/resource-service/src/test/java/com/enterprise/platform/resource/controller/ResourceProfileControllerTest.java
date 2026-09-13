package com.enterprise.platform.resource.controller;

import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.PagedResponse;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.dto.response.WorkloadReportResponse;
import com.enterprise.platform.resource.enums.ResourceStatus;
import com.enterprise.platform.resource.exception.GlobalExceptionHandler;
import com.enterprise.platform.resource.exception.ResourceNotFoundException;
import com.enterprise.platform.resource.service.ResourceProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResourceProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResourceProfileService profileService;

    @InjectMocks
    private ResourceProfileController profileController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID profileId;
    private UUID userId;
    private ResourceProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        profileId = UUID.randomUUID();
        userId = UUID.randomUUID();

        profileResponse = ResourceProfileResponse.builder()
                .id(profileId)
                .organizationId(UUID.randomUUID())
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .skills("Java, Spring Boot, Kafka, Docker")
                .hourlyRate(new BigDecimal("120.00"))
                .currency("USD")
                .status(ResourceStatus.AVAILABLE)
                .totalAllocatedPercentage(BigDecimal.ZERO)
                .allocatedHoursPerWeek(BigDecimal.ZERO)
                .remainingCapacityHours(new BigDecimal("40.00"))
                .isOverAllocated(false)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/resources - Should create resource profile")
    void testCreateProfile() throws Exception {
        CreateResourceProfileRequest request = CreateResourceProfileRequest.builder()
                .userId(userId)
                .jobTitle("Principal Java Architect")
                .weeklyCapacityHours(new BigDecimal("40.00"))
                .skills("Java, Spring Boot")
                .build();

        when(profileService.createProfile(any(CreateResourceProfileRequest.class))).thenReturn(profileResponse);

        mockMvc.perform(post("/api/v1/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(profileId.toString()))
                .andExpect(jsonPath("$.data.jobTitle").value("Principal Java Architect"));
    }

    @Test
    @DisplayName("GET /api/v1/resources/{id} - Should return resource profile")
    void testGetProfileById() throws Exception {
        when(profileService.getProfileById(profileId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/resources/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(profileId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/resources/{id} - Should return 404 when not found")
    void testGetProfileById_NotFound() throws Exception {
        when(profileService.getProfileById(profileId))
                .thenThrow(new ResourceNotFoundException("Resource profile not found with ID: " + profileId));

        mockMvc.perform(get("/api/v1/resources/{id}", profileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resource profile not found with ID: " + profileId));
    }

    @Test
    @DisplayName("GET /api/v1/resources/user/{userId} - Should return profile by user ID")
    void testGetProfileByUserId() throws Exception {
        when(profileService.getProfileByUserId(userId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/resources/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/resources - Should return paginated profiles")
    void testGetProfiles() throws Exception {
        PagedResponse<ResourceProfileResponse> pagedResponse = PagedResponse.<ResourceProfileResponse>builder()
                .content(List.of(profileResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(profileService.getProfiles(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(profileId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/resources/skills/search - Should search by skill")
    void testSearchBySkill() throws Exception {
        when(profileService.searchBySkill("Java")).thenReturn(List.of(profileResponse));

        mockMvc.perform(get("/api/v1/resources/skills/search")
                        .param("skill", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].skills").value("Java, Spring Boot, Kafka, Docker"));
    }

    @Test
    @DisplayName("GET /api/v1/resources/reports/workload - Should return workload report")
    void testGetWorkloadReport() throws Exception {
        WorkloadReportResponse report = WorkloadReportResponse.builder()
                .organizationId(UUID.randomUUID())
                .totalResources(1)
                .totalAllocatedResources(0)
                .overAllocatedResources(0)
                .averageUtilizationPercentage(BigDecimal.ZERO)
                .resources(Collections.emptyList())
                .build();

        when(profileService.getWorkloadReport()).thenReturn(report);

        mockMvc.perform(get("/api/v1/resources/reports/workload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalResources").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/resources/{id} - Should update profile")
    void testUpdateProfile() throws Exception {
        UpdateResourceProfileRequest request = UpdateResourceProfileRequest.builder()
                .jobTitle("Principal Architect Lead")
                .build();

        when(profileService.updateProfile(eq(profileId), any(UpdateResourceProfileRequest.class))).thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/resources/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/resources/{id} - Should delete profile")
    void testDeleteProfile() throws Exception {
        doNothing().when(profileService).deleteProfile(profileId);

        mockMvc.perform(delete("/api/v1/resources/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource profile deleted successfully"));
    }
}
