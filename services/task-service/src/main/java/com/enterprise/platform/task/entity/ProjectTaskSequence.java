package com.enterprise.platform.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_task_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTaskSequence {

    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "project_key", nullable = false, length = 20)
    private String projectKey;

    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
