package io.github.bluething.pathpulse.storageservice.kafka;

import io.github.bluething.pathpulse.storageservice.model.LocationEntity;
import io.github.bluething.pathpulse.storageservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
class LocationEventConsumer {
    private final LocationService locationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.location-events}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consume(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.debug("Received message: partition={}, offset={}", partition, offset);

        try {
            LocationEntity document = parseMessage(message);
            locationService.save(document);

            acknowledgment.acknowledge();
            log.debug("Successfully processed and acknowledged: eventId={}", document.eventId());

        } catch (Exception e) {
            log.error("Failed to process message: partition={}, offset={}, error={}",
                    partition, offset, e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
            throw new LocationEventProcessingException("Failed to process location event", e);
        }
    }

    private LocationEntity parseMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            return LocationEntity.fromEvent(
                    node.get("eventId").asText(),
                    node.get("userId").asText(),
                    node.get("latitude").asDouble(),
                    node.get("longitude").asDouble(),
                    node.get("accuracy").asDouble(),
                    Instant.parse(node.get("clientTimestamp").asText()),
                    Instant.parse(node.get("serverTimestamp").asText()),
                    node.get("label").asText());
        } catch (Exception e) {
            throw new LocationEventProcessingException("Failed to parse message", e);
        }
    }

    /**
     * Exception for location event processing failures.
     */
    public static class LocationEventProcessingException extends RuntimeException {
        public LocationEventProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
