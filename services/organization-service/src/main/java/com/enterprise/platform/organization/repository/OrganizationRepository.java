package com.enterprise.platform.organization.repository;

import com.enterprise.platform.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Page<Organization> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(
            String name,
            String slug,
            Pageable pageable
    );
}
