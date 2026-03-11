package io.github.bluething.pathpulse.ingestionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public record LocationData(
        String userId,
        double latitude,
        double longitude,
        double accuracy,
        Instant timestamp) {

    /**
     * Returns null if data is valid, or a descriptive error message identifying
     * the first field that fails validation.
     */
    @JsonIgnore
    public String validationError() {
        if (userId == null || userId.isBlank()) return "userId is missing or blank";
        if (latitude < -90 || latitude > 90) return "latitude out of range: " + latitude;
        if (longitude < -180 || longitude > 180) return "longitude out of range: " + longitude;
        if (accuracy < 0) return "accuracy must be non-negative: " + accuracy;
        if (timestamp == null) return "timestamp is missing";
        return null;
    }

    /**
     * Convenience wrapper around {@link #validationError()}.
     */
    @JsonIgnore
    public boolean isValid() {
        return validationError() == null;
    }
}
