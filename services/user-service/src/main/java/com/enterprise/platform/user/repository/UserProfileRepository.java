package com.enterprise.platform.user.repository;

import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.dto.response.UserProfileResponse;
import com.enterprise.platform.user.entity.UserProfile;
import com.enterprise.platform.user.specification.UserProfileSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository
        extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(
            UUID userId
    );

    Optional<UserProfile> findByEmployeeCode(
            String employeeCode
    );

    Page<UserProfile> findAll(Specification<UserProfile> specification, Pageable pageable);

}