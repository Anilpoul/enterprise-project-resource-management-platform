package com.enterprise.platform.auth.repository;

import com.enterprise.platform.auth.constant.enums.RoleType;
import com.enterprise.platform.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType roleType);

}