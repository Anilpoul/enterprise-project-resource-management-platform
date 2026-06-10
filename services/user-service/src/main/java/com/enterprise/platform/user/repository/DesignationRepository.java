package com.enterprise.platform.user.repository;

import com.enterprise.platform.user.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository
        extends JpaRepository<Designation, UUID> {

    Optional<Designation> findByName(String name);

    boolean existsByName(String name);
}
