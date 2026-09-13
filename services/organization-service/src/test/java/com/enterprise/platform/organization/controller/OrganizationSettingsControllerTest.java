package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.dto.request.OrganizationSettingsRequest;
import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;
import com.enterprise.platform.organization.exception.GlobalExceptionHandler;
import com.enterprise.platform.organization.service.OrganizationSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrganizationSettingsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganizationSettingsService settingsService;

    @InjectMocks
    private OrganizationSettingsController settingsController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID orgId;
    private OrganizationSettingsResponse settingsResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        orgId = UUID.randomUUID();
        settingsResponse = OrganizationSettingsResponse.builder()
                .id(UUID.randomUUID())
                .organizationId(orgId)
                .timezone("UTC")
                .dateFormat("YYYY-MM-DD")
                .allowExternalSharing(false)
                .mfaRequired(false)
                .maxProjects(100)
                .maxUsers(500)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/settings - Success returns 200")
    void testGetSettings_Success() throws Exception {
        when(settingsService.getSettings(orgId)).thenReturn(settingsResponse);

        mockMvc.perform(get("/api/v1/organizations/" + orgId + "/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timezone").value("UTC"))
                .andExpect(jsonPath("$.data.maxUsers").value(500));
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{orgId}/settings - Success returns 200")
    void testUpdateSettings_Success() throws Exception {
        OrganizationSettingsRequest request = OrganizationSettingsRequest.builder()
                .timezone("America/New_York")
                .maxUsers(1000)
                .build();

        OrganizationSettingsResponse updated = OrganizationSettingsResponse.builder()
                .id(settingsResponse.getId())
                .organizationId(orgId)
                .timezone("America/New_York")
                .dateFormat("YYYY-MM-DD")
                .maxUsers(1000)
                .build();

        when(settingsService.updateSettings(eq(orgId), any(OrganizationSettingsRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/organizations/" + orgId + "/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.maxUsers").value(1000));
    }
}
