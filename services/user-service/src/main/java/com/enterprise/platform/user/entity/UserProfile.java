package com.enterprise.platform.user.entity;

import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(
        name = "user_profiles",
        indexes = {
                @Index(
                        name = "idx_user_profile_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_profile_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_user_profile_manager_id",
                        columnList = "manager_id"
                )
        }
)
@Getter
@Setter
public class UserProfile extends BaseEntity {

    @Column(
            name = "auth_user_id",
            nullable = false,
            unique = true
    )
    private UUID userId;

    @Column(unique = true, length = 50)
    private String employeeCode;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    private String profileImageUrl;

    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserProfileStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    private UUID managerId;
}