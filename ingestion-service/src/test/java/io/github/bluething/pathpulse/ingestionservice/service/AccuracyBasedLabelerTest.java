package io.github.bluething.pathpulse.ingestionservice.service;

import io.github.bluething.pathpulse.ingestionservice.model.LocationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccuracyBasedLabelerTest {

    private AccuracyBasedLabeler labeler;

    @BeforeEach
    void setUp() {
        labeler = new AccuracyBasedLabeler();
    }

    @Test
    @DisplayName("should label as HIGH_PRECISION for accuracy <= 10")
    void shouldLabelHighPrecision() {
        var data = createLocationData(5.0);
        assertEquals("HIGH_PRECISION", labeler.label(data));

        var data2 = createLocationData(10.0);
        assertEquals("HIGH_PRECISION", labeler.label(data2));
    }

    @Test
    @DisplayName("should label as MEDIUM_PRECISION for accuracy > 10 and <= 50")
    void shouldLabelMediumPrecision() {
        var data = createLocationData(11.0);
        assertEquals("MEDIUM_PRECISION", labeler.label(data));

        var data2 = createLocationData(50.0);
        assertEquals("MEDIUM_PRECISION", labeler.label(data2));
    }

    @Test
    @DisplayName("should label as LOW_PRECISION for accuracy > 50")
    void shouldLabelLowPrecision() {
        var data = createLocationData(51.0);
        assertEquals("LOW_PRECISION", labeler.label(data));

        var data2 = createLocationData(100.0);
        assertEquals("LOW_PRECISION", labeler.label(data2));
    }

    private LocationData createLocationData(double accuracy) {
        return new LocationData(
                "user-123",
                37.7749,
                -122.4194,
                accuracy,
                Instant.now());
    }
}