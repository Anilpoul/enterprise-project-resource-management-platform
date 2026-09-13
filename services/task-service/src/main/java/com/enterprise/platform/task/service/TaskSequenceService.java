package com.enterprise.platform.task.service;

import java.util.UUID;

public interface TaskSequenceService {

    String generateNextTaskKey(UUID projectId, String projectKey);
}
