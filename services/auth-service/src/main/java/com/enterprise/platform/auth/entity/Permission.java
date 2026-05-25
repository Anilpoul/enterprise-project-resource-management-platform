package com.enterprise.platform.auth.entity;

import com.enterprise.platform.auth.constant.enums.PermissionType;
import com.enterprise.platform.auth.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PermissionType name;

    @Column(length = 500)
    private String description;

}