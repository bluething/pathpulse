package io.github.bluething.pathpulse.ingestionservice.kafka;

import io.github.bluething.pathpulse.ingestionservice.model.LocationEvent;
import reactor.core.publisher.Mono;

public interface LocationEventPublisher {
    Mono<Void> publish(LocationEvent event);
}
