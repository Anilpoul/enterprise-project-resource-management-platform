package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.task.entity.ProjectTaskSequence;
import com.enterprise.platform.task.repository.ProjectTaskSequenceRepository;
import com.enterprise.platform.task.service.TaskSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSequenceServiceImpl implements TaskSequenceService {

    private final ProjectTaskSequenceRepository sequenceRepository;

    @Override
    @Transactional
    public String generateNextTaskKey(UUID projectId, String projectKey) {
        String normalizedKey = projectKey != null ? projectKey.trim().toUpperCase() : "TASK";
        log.debug("Generating next task key for project: {}, key prefix: {}", projectId, normalizedKey);

        ProjectTaskSequence sequence = sequenceRepository.findByProjectIdForUpdate(projectId)
                .orElseGet(() -> {
                    log.info("Initializing task sequence for project: {} with key prefix: {}", projectId, normalizedKey);
                    ProjectTaskSequence init = ProjectTaskSequence.builder()
                            .projectId(projectId)
                            .projectKey(normalizedKey)
                            .currentSequence(0L)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return sequenceRepository.saveAndFlush(init);
                });

        long nextSequence = sequence.getCurrentSequence() + 1;
        sequence.setCurrentSequence(nextSequence);
        sequence.setProjectKey(normalizedKey);
        sequence.setUpdatedAt(LocalDateTime.now());
        sequenceRepository.save(sequence);

        String taskKey = normalizedKey + "-" + nextSequence;
        log.info("Generated task key: {} for project: {}", taskKey, projectId);
        return taskKey;
    }
}
