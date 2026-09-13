package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.constants.enums.MemberStatus;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
import com.enterprise.platform.organization.dto.request.AddMemberRequest;
import com.enterprise.platform.organization.dto.request.UpdateMemberRoleRequest;
import com.enterprise.platform.organization.dto.response.OrganizationMemberResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.exception.GlobalExceptionHandler;
import com.enterprise.platform.organization.service.OrganizationMemberService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrganizationMemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganizationMemberService memberService;

    @InjectMocks
    private OrganizationMemberController memberController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID orgId;
    private UUID userId;
    private OrganizationMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        orgId = UUID.randomUUID();
        userId = UUID.randomUUID();

        memberResponse = OrganizationMemberResponse.builder()
                .id(UUID.randomUUID())
                .organizationId(orgId)
                .userId(userId)
                .role(OrganizationRole.ORG_MEMBER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/members - Success returns 201")
    void testAddMember_Success() throws Exception {
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(userId)
                .role(OrganizationRole.ORG_MEMBER)
                .build();

        when(memberService.addMember(eq(orgId), any(AddMemberRequest.class))).thenReturn(memberResponse);

        mockMvc.perform(post("/api/v1/organizations/" + orgId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ORG_MEMBER"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/members - Success returns 200 with paged response")
    void testGetMembers_Success() throws Exception {
        PagedResponse<OrganizationMemberResponse> pagedResponse = PagedResponse.<OrganizationMemberResponse>builder()
                .content(List.of(memberResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(memberService.getMembers(eq(orgId), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/organizations/" + orgId + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(userId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/members/{userId} - Success returns 200")
    void testGetMember_Success() throws Exception {
        when(memberService.getMember(orgId, userId)).thenReturn(memberResponse);

        mockMvc.perform(get("/api/v1/organizations/" + orgId + "/members/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{orgId}/members/{userId}/role - Success returns 200")
    void testUpdateMemberRole_Success() throws Exception {
        UpdateMemberRoleRequest request = UpdateMemberRoleRequest.builder()
                .role(OrganizationRole.ORG_MANAGER)
                .build();

        OrganizationMemberResponse updated = OrganizationMemberResponse.builder()
                .id(memberResponse.getId())
                .organizationId(orgId)
                .userId(userId)
                .role(OrganizationRole.ORG_MANAGER)
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberService.updateMemberRole(eq(orgId), eq(userId), any(UpdateMemberRoleRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/organizations/" + orgId + "/members/" + userId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ORG_MANAGER"));
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{orgId}/members/{userId} - Success returns 200")
    void testRemoveMember_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/organizations/" + orgId + "/members/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Member removed successfully"));

        verify(memberService).removeMember(orgId, userId);
    }
}
