package com.enterprise.platform.user.repository;

import com.enterprise.platform.user.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, UUID> {

    boolean existsByNameIgnoreCase(
            String name
    );

    Page<Department> findByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}
