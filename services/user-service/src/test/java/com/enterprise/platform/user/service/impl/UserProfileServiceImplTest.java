package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.events.AuthEventType;
import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import com.enterprise.platform.user.dto.response.UserProfileResponse;
import com.enterprise.platform.user.entity.UserProfile;
import com.enterprise.platform.user.exception.BadRequestException;
import com.enterprise.platform.user.exception.ResourceNotFoundException;
import com.enterprise.platform.user.mapper.UserProfileMapper;
import com.enterprise.platform.user.repository.DepartmentRepository;
import com.enterprise.platform.user.repository.DesignationRepository;
import com.enterprise.platform.user.repository.UserProfileRepository;
import com.enterprise.platform.user.service.EmployeeCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private EmployeeCodeGenerator employeeCodeGenerator;

    @Captor
    private ArgumentCaptor<UserProfile> userProfileCaptor;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID userId;
    private AuthEvent registerEvent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        registerEvent = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_REGISTERED)
                .userId(userId)
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .timestamp(LocalDateTime.now())
                .details("Registered")
                .build();
    }

    @Test
    @DisplayName("Should create user profile with generated employee code on USER_REGISTERED event")
    void testCreateUserProfileSuccess() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(employeeCodeGenerator.generate()).thenReturn("EMP000001");

        userProfileService.createUserProfile(registerEvent);

        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertThat(savedProfile.getUserId()).isEqualTo(userId);
        assertThat(savedProfile.getFirstName()).isEqualTo("Bob");
        assertThat(savedProfile.getLastName()).isEqualTo("Jones");
        assertThat(savedProfile.getEmail()).isEqualTo("bob@example.com");
        assertThat(savedProfile.getEmployeeCode()).isEqualTo("EMP000001");
        assertThat(savedProfile.getStatus()).isEqualTo(UserProfileStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should be idempotent and not duplicate profile if user already exists")
    void testCreateUserProfileIdempotent() {
        UserProfile existing = new UserProfile();
        existing.setUserId(userId);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        userProfileService.createUserProfile(registerEvent);

        verify(userProfileRepository, never()).save(any());
        verify(employeeCodeGenerator, never()).generate();
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void testGetUserProfileSuccess() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFirstName("Bob");
        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .userId(userId)
                .firstName("Bob")
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileMapper.toResponse(profile)).thenReturn(expectedResponse);

        UserProfileResponse response = userProfileService.getUserProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user profile does not exist")
    void testGetUserProfileNotFound() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getUserProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User profile not found");
    }

    @Test
    @DisplayName("Should throw BadRequestException when activating a terminated user")
    void testActivateTerminatedUser() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setStatus(UserProfileStatus.TERMINATED);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> userProfileService.activateUser(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Terminated user cannot be activated");
    }

    @Test
    @DisplayName("Should activate inactive user successfully")
    void testActivateUserSuccess() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setStatus(UserProfileStatus.INACTIVE);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        userProfileService.activateUser(userId);

        assertThat(profile.getStatus()).isEqualTo(UserProfileStatus.ACTIVE);
    }
}
