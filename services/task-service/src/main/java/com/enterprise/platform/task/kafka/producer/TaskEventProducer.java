package com.enterprise.platform.task.kafka.producer;

import com.enterprise.platform.events.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.task-events:task-events}")
    private String taskEventsTopic;

    public void publish(TaskEvent event) {
        String key = event.getTaskId() != null ? event.getTaskId().toString() : event.getEventId().toString();
        log.info("Publishing task event: {} for task: {} to topic: {}", event.getEventType(), key, taskEventsTopic);
        try {
            kafkaTemplate.send(taskEventsTopic, key, event);
        } catch (Exception e) {
            log.error("Failed to publish task event: {}", event.getEventType(), e);
        }
    }
}
