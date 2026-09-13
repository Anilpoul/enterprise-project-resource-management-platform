package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.task.entity.ProjectTaskSequence;
import com.enterprise.platform.task.repository.ProjectTaskSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSequenceServiceImplTest {

    @Mock
    private ProjectTaskSequenceRepository sequenceRepository;

    @InjectMocks
    private TaskSequenceServiceImpl sequenceService;

    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should increment existing sequence and return formatted key")
    void testGenerateNextTaskKey_ExistingSequence() {
        ProjectTaskSequence sequence = ProjectTaskSequence.builder()
                .projectId(projectId)
                .projectKey("CORE")
                .currentSequence(42L)
                .updatedAt(LocalDateTime.now())
                .build();

        when(sequenceRepository.findByProjectIdForUpdate(projectId)).thenReturn(Optional.of(sequence));
        when(sequenceRepository.save(any(ProjectTaskSequence.class))).thenReturn(sequence);

        String taskKey = sequenceService.generateNextTaskKey(projectId, "CORE");

        assertThat(taskKey).isEqualTo("CORE-43");
        assertThat(sequence.getCurrentSequence()).isEqualTo(43L);
        verify(sequenceRepository).save(sequence);
    }

    @Test
    @DisplayName("Should initialize new sequence when none exists and return key-1")
    void testGenerateNextTaskKey_NewSequence() {
        ProjectTaskSequence newSequence = ProjectTaskSequence.builder()
                .projectId(projectId)
                .projectKey("PAY")
                .currentSequence(0L)
                .updatedAt(LocalDateTime.now())
                .build();

        when(sequenceRepository.findByProjectIdForUpdate(projectId)).thenReturn(Optional.empty());
        when(sequenceRepository.saveAndFlush(any(ProjectTaskSequence.class))).thenReturn(newSequence);
        when(sequenceRepository.save(any(ProjectTaskSequence.class))).thenReturn(newSequence);

        String taskKey = sequenceService.generateNextTaskKey(projectId, "pay");

        assertThat(taskKey).isEqualTo("PAY-1");
        assertThat(newSequence.getCurrentSequence()).isEqualTo(1L);
        verify(sequenceRepository).saveAndFlush(any(ProjectTaskSequence.class));
        verify(sequenceRepository).save(newSequence);
    }
}
