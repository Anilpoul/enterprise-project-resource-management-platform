package com.enterprise.platform.user.repository;

import com.enterprise.platform.user.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DesignationRepository
        extends JpaRepository<Designation, UUID> {

    boolean existsByNameIgnoreCase(
            String name
    );

    Page<Designation> findByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}