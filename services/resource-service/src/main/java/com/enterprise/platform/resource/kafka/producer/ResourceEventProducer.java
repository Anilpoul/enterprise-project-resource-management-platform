package com.enterprise.platform.resource.kafka.producer;

import com.enterprise.platform.events.ResourceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.resource-events:resource-events}")
    private String resourceEventsTopic;

    public void publish(ResourceEvent event) {
        String key = event.getResourceId() != null ? event.getResourceId().toString() : event.getEventId().toString();
        log.info("Publishing resource event: {} for resource: {} to topic: {}", event.getEventType(), key, resourceEventsTopic);
        try {
            kafkaTemplate.send(resourceEventsTopic, key, event);
        } catch (Exception e) {
            log.error("Failed to publish resource event: {}", event.getEventType(), e);
        }
    }
}
