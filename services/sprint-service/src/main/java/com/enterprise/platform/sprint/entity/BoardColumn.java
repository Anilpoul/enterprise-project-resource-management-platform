package com.enterprise.platform.sprint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "board_columns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardColumn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "task_status", nullable = false, length = 30)
    private String taskStatus;

    @Column(name = "column_order", nullable = false)
    private Integer columnOrder;

    @Column(name = "wip_limit")
    private Integer wipLimit;
}
