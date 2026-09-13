package com.enterprise.platform.organization.kafka.producer;

import com.enterprise.platform.events.OrganizationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationEventProducer {

    private static final String TOPIC = "organization-events";

    private final KafkaTemplate<String, OrganizationEvent> kafkaTemplate;

    public void publish(OrganizationEvent event) {
        log.info("Publishing organization event: {} for orgId: {}", event.getEventType(), event.getOrganizationId());

        String messageKey = event.getOrganizationId() != null
                ? event.getOrganizationId().toString()
                : UUID.randomUUID().toString();

        kafkaTemplate.send(TOPIC, messageKey, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish organization event: {}", event.getEventType(), ex);
            } else {
                log.info("Organization event published successfully: Topic={}, Partition={}, Offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
