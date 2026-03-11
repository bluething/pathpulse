package io.github.bluething.pathpulse.ingestionservice.websocket;

import tools.jackson.databind.json.JsonMapper;
import io.github.bluething.pathpulse.ingestionservice.service.LocationLabeler;
import io.github.bluething.pathpulse.ingestionservice.exception.InvalidLocationDataException;
import io.github.bluething.pathpulse.ingestionservice.kafka.LocationEventPublisher;
import io.github.bluething.pathpulse.ingestionservice.model.LocationData;
import io.github.bluething.pathpulse.ingestionservice.model.LocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class LocationWebSocketHandler implements WebSocketHandler {

    private final JsonMapper objectMapper;
    private final LocationLabeler labeler;
    private final LocationEventPublisher publisher;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var sessionId = session.getId();
        log.info("WebSocket connection established: sessionId={}", sessionId);

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> processMessage(session, payload))
                .doOnError(e -> log.error("Unhandled error in WebSocket pipeline: sessionId={}", sessionId, e))
                .doFinally(signal -> log.info("WebSocket connection closed: sessionId={}, signal={}",
                        sessionId, signal))
                .then();
    }

    private Mono<Void> processMessage(WebSocketSession session, String payload) {
        return Mono.fromCallable(() -> parseLocationData(payload))
                .flatMap(data -> {
                    String error = data.validationError();
                    if (error != null) {
                        log.warn("Invalid location data from session={}: {}", session.getId(), error);
                        return sendError(session, error).then(Mono.<LocationData>empty());
                    }
                    return Mono.just(data);
                })
                .flatMap(data -> publisher.publish(createLabeledEvent(data)))
                .onErrorResume(InvalidLocationDataException.class, e -> {
                    log.warn("Failed to parse payload from session={}: {}", session.getId(), e.getMessage());
                    return sendError(session, e.getMessage());
                })
                .onErrorResume(e -> {
                    log.error("Failed to publish event for session={}", session.getId(), e);
                    return sendError(session, "Internal server error. Message could not be processed.");
                });
    }

    private Mono<Void> sendError(WebSocketSession session, String message) {
        var json = "{\"error\":\"" + message + "\"}";
        return session.send(Mono.just(session.textMessage(json)));
    }

    private LocationEvent createLabeledEvent(LocationData data) {
        var label = labeler.label(data);
        return LocationEvent.fromLocationData(data, label);
    }

    private LocationData parseLocationData(String payload) {
        try {
            return objectMapper.readValue(payload, LocationData.class);
        } catch (Exception e) {
            log.error("Failed to parse location data", e);
            throw new InvalidLocationDataException("Failed to parse JSON payload", e);
        }
    }
}
