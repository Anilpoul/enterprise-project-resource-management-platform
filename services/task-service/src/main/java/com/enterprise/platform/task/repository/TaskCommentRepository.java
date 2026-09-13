package com.enterprise.platform.task.repository;

import com.enterprise.platform.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    Optional<TaskComment> findByIdAndTaskId(UUID id, UUID taskId);
}
