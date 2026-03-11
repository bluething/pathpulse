package io.github.bluething.pathpulse.ingestionservice.model;

import com.fasterxml.uuid.Generators;

import java.time.Instant;
import java.util.UUID;

public record LocationEvent(
        UUID eventId,
        String userId,
        double latitude,
        double longitude,
        double accuracy,
        Instant clientTimestamp,
        Instant serverTimestamp,
        String label) {
    /**
     * Creates a LocationEvent from LocationData with server-side labeling.
     *
     * @param data  The original location data
     * @param label The classification label
     * @return A new labeled LocationEvent
     */
    public static LocationEvent fromLocationData(LocationData data, String label) {
        return new LocationEvent(
                Generators.timeBasedEpochGenerator().generate(),
                data.userId(),
                data.latitude(),
                data.longitude(),
                data.accuracy(),
                data.timestamp(),
                Instant.now(),
                label);
    }
}
