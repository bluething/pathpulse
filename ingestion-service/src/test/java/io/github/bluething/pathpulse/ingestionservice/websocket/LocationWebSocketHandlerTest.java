package io.github.bluething.pathpulse.ingestionservice.websocket;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bluething.pathpulse.ingestionservice.kafka.LocationEventPublisher;
import io.github.bluething.pathpulse.ingestionservice.model.LocationData;
import io.github.bluething.pathpulse.ingestionservice.model.LocationEvent;
import io.github.bluething.pathpulse.ingestionservice.service.LocationLabeler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationWebSocketHandlerTest {
    @Mock
    private LocationLabeler labeler;

    @Mock
    private LocationEventPublisher publisher;

    @Mock
    private WebSocketSession session;

    @Mock
    private WebSocketMessage message;

    private LocationWebSocketHandler handler;
    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
        handler = new LocationWebSocketHandler(objectMapper, labeler, publisher);
    }

    @Test
    @DisplayName("should process valid location message and publish to Kafka")
    void shouldProcessValidMessage() throws Exception {
        // Given
        var locationData = new LocationData(
                "user-123",
                37.7749,
                -122.4194,
                5.0,
                Instant.now());
        String json = objectMapper.writeValueAsString(locationData);

        when(session.getId()).thenReturn("session-1");
        when(session.receive()).thenReturn(Flux.just(message));
        when(message.getPayloadAsText()).thenReturn(json);
        when(labeler.label(any(LocationData.class))).thenReturn("HIGH_PRECISION");
        when(publisher.publish(any(LocationEvent.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = handler.handle(session);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<LocationEvent> eventCaptor = ArgumentCaptor.forClass(LocationEvent.class);
        verify(publisher).publish(eventCaptor.capture());

        LocationEvent capturedEvent = eventCaptor.getValue();
        assertEquals("user-123", capturedEvent.userId());
        assertEquals("HIGH_PRECISION", capturedEvent.label());
        assertNotNull(capturedEvent.eventId());
        assertNotNull(capturedEvent.serverTimestamp());
    }

    @Test
    @DisplayName("should skip invalid location data without failing")
    void shouldSkipInvalidData() throws Exception {
        // Given - invalid latitude
        String json = """
                {
                    "userId": "",
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "accuracy": 5.0,
                    "timestamp": "2024-01-01T00:00:00Z"
                }
                """;

        when(session.getId()).thenReturn("session-1");
        when(session.receive()).thenReturn(Flux.just(message));
        when(message.getPayloadAsText()).thenReturn(json);
        when(session.textMessage(anyString())).thenReturn(message);
        when(session.send(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = handler.handle(session);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("should handle malformed JSON gracefully")
    void shouldHandleMalformedJson() {
        // Given
        String invalidJson = "{ invalid json }";

        when(session.getId()).thenReturn("session-1");
        when(session.receive()).thenReturn(Flux.just(message));
        when(message.getPayloadAsText()).thenReturn(invalidJson);
        when(session.textMessage(anyString())).thenReturn(message);
        when(session.send(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = handler.handle(session);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(publisher, never()).publish(any());
    }
}