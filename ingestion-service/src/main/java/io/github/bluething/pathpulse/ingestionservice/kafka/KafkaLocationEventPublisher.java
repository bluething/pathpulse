package io.github.bluething.pathpulse.ingestionservice.kafka;

import io.github.bluething.pathpulse.ingestionservice.model.LocationEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class KafkaLocationEventPublisher implements LocationEventPublisher {
    @Override
    public Mono<Void> publish(LocationEvent event) {
        return null;
    }
}
