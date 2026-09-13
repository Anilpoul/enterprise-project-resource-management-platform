package com.enterprise.platform.organization.repository;

import com.enterprise.platform.organization.entity.OrganizationMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Page<OrganizationMember> findByOrganizationId(UUID organizationId, Pageable pageable);

    List<OrganizationMember> findByUserId(UUID userId);

    long countByOrganizationId(UUID organizationId);
}
