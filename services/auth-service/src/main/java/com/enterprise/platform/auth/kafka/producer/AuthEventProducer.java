package com.enterprise.platform.auth.kafka.producer;

import com.enterprise.platform.auth.kafka.event.AuthEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventProducer {

    private static final String AUTH_TOPIC = "auth-events";

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;

    public void publish(AuthEvent authEvent) {

        log.info(
                "Publishing auth event: {}",
                authEvent.getEventType()
        );

        kafkaTemplate.send(
                AUTH_TOPIC,
                authEvent
        ).whenComplete((result, ex) -> {

            if (ex != null) {
                log.error(
                        "Kafka publish failed",
                        ex
                );
            } else {
                log.info(
                        "Kafka message sent successfully"
                );
            }
        });
    }

}