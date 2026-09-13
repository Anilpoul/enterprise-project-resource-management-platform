package com.enterprise.platform.sprint.kafka.producer;

import com.enterprise.platform.events.SprintEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SprintEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.sprint-events:sprint-events}")
    private String sprintEventsTopic;

    public void publish(SprintEvent event) {
        String key = event.getSprintId() != null ? event.getSprintId().toString() : event.getEventId().toString();
        log.info("Publishing sprint event: {} for sprint: {} to topic: {}", event.getEventType(), key, sprintEventsTopic);
        try {
            kafkaTemplate.send(sprintEventsTopic, key, event);
        } catch (Exception e) {
            log.error("Failed to publish sprint event: {}", event.getEventType(), e);
        }
    }
}
