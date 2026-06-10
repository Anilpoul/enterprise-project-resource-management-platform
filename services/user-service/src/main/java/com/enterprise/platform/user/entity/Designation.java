package com.enterprise.platform.user.entity;

import com.enterprise.platform.user.constants.enums.DesignationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "designations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_designation_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
public class Designation extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DesignationStatus status;
}
