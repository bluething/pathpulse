package io.github.bluething.pathpulse.ingestionservice.kafka;

import io.github.bluething.pathpulse.ingestionservice.model.LocationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
class KafkaLocationEventPublisher implements LocationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public KafkaLocationEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${kafka.topic.location-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    @Override
    public Mono<Void> publish(LocationEvent event) {
        return Mono.fromCallable(() -> serializeEvent(event))
                .flatMap(json -> sendToKafka(event.userId(), json))
                .doOnSuccess(v -> log.debug("Published event: eventId={}, userId={}",
                        event.eventId(), event.userId()))
                .doOnError(e -> log.error("Failed to publish event: eventId={}, userId={}, error={}",
                        event.eventId(), event.userId(), e.getMessage()))
                .then();
    }

    private String serializeEvent(LocationEvent event) {
        return objectMapper.writeValueAsString(event);
    }

    private Mono<Void> sendToKafka(String key, String value) {
        return Mono.fromFuture(kafkaTemplate.send(topicName, key, value))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
