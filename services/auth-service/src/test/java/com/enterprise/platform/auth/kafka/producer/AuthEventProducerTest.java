package com.enterprise.platform.auth.kafka.producer;

import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.events.AuthEventType;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthEventProducerTest {

    @Mock
    private KafkaTemplate<String, AuthEvent> kafkaTemplate;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<AuthEvent> eventCaptor;

    private AuthEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new AuthEventProducer(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish event with userId as key when userId is present")
    void testPublishWithUserId() {
        UUID userId = UUID.randomUUID();
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_REGISTERED)
                .userId(userId)
                .email("test@example.com")
                .timestamp(LocalDateTime.now())
                .details("Registration")
                .build();

        CompletableFuture<SendResult<String, AuthEvent>> future = CompletableFuture.completedFuture(
                new SendResult<>(null, new RecordMetadata(new TopicPartition("auth-events", 0), 0, 0, 0, 0, 0))
        );
        when(kafkaTemplate.send(anyString(), anyString(), any(AuthEvent.class))).thenReturn(future);

        assertDoesNotThrow(() -> producer.publish(event));

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("auth-events");
        assertThat(keyCaptor.getValue()).isEqualTo(userId.toString());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(AuthEventType.USER_REGISTERED);
    }

    @Test
    @DisplayName("Should publish event safely without NPE when userId is null (e.g. login failed)")
    void testPublishWithoutUserId() {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.LOGIN_FAILED)
                .userId(null)
                .email("unknown@example.com")
                .timestamp(LocalDateTime.now())
                .details("Invalid credentials")
                .build();

        CompletableFuture<SendResult<String, AuthEvent>> future = CompletableFuture.completedFuture(
                new SendResult<>(null, new RecordMetadata(new TopicPartition("auth-events", 0), 0, 0, 0, 0, 0))
        );
        when(kafkaTemplate.send(anyString(), anyString(), any(AuthEvent.class))).thenReturn(future);

        assertDoesNotThrow(() -> producer.publish(event));

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("auth-events");
        assertThat(keyCaptor.getValue()).isEqualTo("unknown@example.com");
    }

    @Test
    @DisplayName("Should publish event safely when both userId and email are null")
    void testPublishWithoutUserIdOrEmail() {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_LOGGED_OUT)
                .userId(null)
                .email(null)
                .timestamp(LocalDateTime.now())
                .details("Logged out")
                .build();

        CompletableFuture<SendResult<String, AuthEvent>> future = CompletableFuture.completedFuture(
                new SendResult<>(null, new RecordMetadata(new TopicPartition("auth-events", 0), 0, 0, 0, 0, 0))
        );
        when(kafkaTemplate.send(anyString(), anyString(), any(AuthEvent.class))).thenReturn(future);

        assertDoesNotThrow(() -> producer.publish(event));

        verify(kafkaTemplate).send(eq("auth-events"), keyCaptor.capture(), eq(event));
        assertThat(keyCaptor.getValue()).isNotBlank();
    }
}
