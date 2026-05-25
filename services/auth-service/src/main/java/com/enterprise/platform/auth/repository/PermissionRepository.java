package com.enterprise.platform.auth.repository;

import com.enterprise.platform.auth.constant.enums.PermissionType;
import com.enterprise.platform.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(PermissionType permissionType);

}