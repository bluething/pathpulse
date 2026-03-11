package io.github.bluething.pathpulse.ingestionservice.service;

import io.github.bluething.pathpulse.ingestionservice.model.LocationData;
import org.springframework.stereotype.Service;

@Service
class AccuracyBasedLabeler implements LocationLabeler {
    private static final double HIGH_ACCURACY_THRESHOLD = 10.0;
    private static final double MEDIUM_ACCURACY_THRESHOLD = 50.0;

    @Override
    public String label(LocationData data) {
        // Using pattern matching with switch expression (stable since Java 21)
        return switch (classifyAccuracy(data.accuracy())) {
            case HIGH -> "HIGH_PRECISION";
            case MEDIUM -> "MEDIUM_PRECISION";
            case LOW -> "LOW_PRECISION";
        };
    }

    private AccuracyLevel classifyAccuracy(double accuracy) {
        if (accuracy <= HIGH_ACCURACY_THRESHOLD) {
            return AccuracyLevel.HIGH;
        } else if (accuracy <= MEDIUM_ACCURACY_THRESHOLD) {
            return AccuracyLevel.MEDIUM;
        } else {
            return AccuracyLevel.LOW;
        }
    }

    private enum AccuracyLevel {
        HIGH, MEDIUM, LOW
    }
}
