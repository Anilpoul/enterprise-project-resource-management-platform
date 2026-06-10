package com.enterprise.platform.user.kafka.consumer;

import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final ObjectMapper objectMapper;

    private final UserProfileService userProfileService;

    @KafkaListener(
            topics = "auth-events",
            groupId = "user-service-group"
    )
    public void consume(AuthEvent event) {

        log.info(
                "Received event {} for {}",
                event.getEventType(),
                event.getEmail()
        );

        switch (event.getEventType()) {

            case USER_REGISTERED ->
                    userProfileService.createUserProfile(event);

            default ->
                    log.info(
                            "Ignoring event {}",
                            event.getEventType()
                    );
        }
    }
}
