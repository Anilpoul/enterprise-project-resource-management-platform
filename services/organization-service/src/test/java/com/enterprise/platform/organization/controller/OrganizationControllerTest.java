package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
import com.enterprise.platform.organization.dto.request.CreateOrganizationRequest;
import com.enterprise.platform.organization.dto.request.UpdateOrganizationRequest;
import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.exception.ConflictException;
import com.enterprise.platform.organization.exception.GlobalExceptionHandler;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationController organizationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID orgId;
    private OrganizationResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(organizationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        orgId = UUID.randomUUID();
        response = OrganizationResponse.builder()
                .id(orgId)
                .name("Acme Corp")
                .slug("acme-corp")
                .description("Acme Corp description")
                .status(OrganizationStatus.ACTIVE)
                .memberCount(1L)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/organizations - Success returns 201 CREATED")
    void testCreateOrganization_Success() throws Exception {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Acme Corp")
                .slug("acme-corp")
                .description("Acme Corp description")
                .adminUserId(UUID.randomUUID())
                .build();

        when(organizationService.createOrganization(any(CreateOrganizationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Acme Corp"))
                .andExpect(jsonPath("$.data.slug").value("acme-corp"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations - Validation failure returns 400")
    void testCreateOrganization_ValidationFailure() throws Exception {
        CreateOrganizationRequest invalidRequest = CreateOrganizationRequest.builder()
                .name("")
                .slug("INVALID SLUG WITH SPACES")
                .build();

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/organizations - Conflict returns 409")
    void testCreateOrganization_Conflict() throws Exception {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Acme Corp")
                .slug("acme-corp")
                .adminUserId(UUID.randomUUID())
                .build();

        when(organizationService.createOrganization(any(CreateOrganizationRequest.class)))
                .thenThrow(new ConflictException("Organization with name 'Acme Corp' already exists"));

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Organization with name 'Acme Corp' already exists"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{id} - Success returns 200")
    void testGetOrganizationById_Success() throws Exception {
        when(organizationService.getOrganizationById(orgId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizations/" + orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orgId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{id} - Not found returns 404")
    void testGetOrganizationById_NotFound() throws Exception {
        when(organizationService.getOrganizationById(orgId))
                .thenThrow(new ResourceNotFoundException("Organization not found with ID: " + orgId));

        mockMvc.perform(get("/api/v1/organizations/" + orgId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/slug/{slug} - Success returns 200")
    void testGetOrganizationBySlug_Success() throws Exception {
        when(organizationService.getOrganizationBySlug("acme-corp")).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizations/slug/acme-corp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("acme-corp"));
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{id} - Success returns 200")
    void testUpdateOrganization_Success() throws Exception {
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Acme Updated")
                .description("Updated Description")
                .build();

        when(organizationService.updateOrganization(eq(orgId), any(UpdateOrganizationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/organizations/" + orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/organizations/{id}/status - Success returns 200")
    void testUpdateStatus_Success() throws Exception {
        when(organizationService.updateStatus(orgId, OrganizationStatus.SUSPENDED)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/organizations/" + orgId + "/status")
                        .param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/organizations - Success returns 200 with paged response")
    void testSearchOrganizations_Success() throws Exception {
        PagedResponse<OrganizationResponse> pagedResponse = PagedResponse.<OrganizationResponse>builder()
                .content(List.of(response))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(organizationService.searchOrganizations(eq("acme"), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("search", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Acme Corp"));
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{id} - Success returns 200")
    void testDeleteOrganization_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/organizations/" + orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Organization archived successfully"));
    }
}
