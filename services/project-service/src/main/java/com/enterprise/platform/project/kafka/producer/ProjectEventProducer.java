package com.enterprise.platform.project.kafka.producer;

import com.enterprise.platform.events.ProjectEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectEventProducer {

    private static final String TOPIC = "project-events";

    private final KafkaTemplate<String, ProjectEvent> kafkaTemplate;

    public void publish(ProjectEvent event) {
        log.info("Publishing project event: {} for projectId: {}", event.getEventType(), event.getProjectId());

        String messageKey = event.getProjectId() != null
                ? event.getProjectId().toString()
                : UUID.randomUUID().toString();

        kafkaTemplate.send(TOPIC, messageKey, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish project event: {}", event.getEventType(), ex);
            } else {
                log.info("Project event published successfully: Topic={}, Partition={}, Offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
