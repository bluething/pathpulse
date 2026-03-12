package io.github.bluething.pathpulse.storageservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "location_events")
@EqualsAndHashCode
@Getter
@Setter
public class LocationEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "accuracy", nullable = false)
    private double accuracy;

    @Column(name = "client_timestamp", nullable = false)
    private Instant clientTimestamp;

    @Column(name = "server_timestamp", nullable = false)
    private Instant serverTimestamp;

    @Column(name = "label")
    private String label;

    public LocationEntity() {
    }

    public LocationEntity(String id, String eventId, String userId, double latitude, double longitude, double accuracy,
                          Instant clientTimestamp, Instant serverTimestamp, String label) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.clientTimestamp = clientTimestamp;
        this.serverTimestamp = serverTimestamp;
        this.label = label;
    }

    public static LocationEntity fromEvent(
            String eventId,
            String userId,
            double latitude,
            double longitude,
            double accuracy,
            Instant clientTimestamp,
            Instant serverTimestamp,
            String label) {
        return new LocationEntity(
                eventId, // use eventId as PK
                eventId,
                userId,
                latitude,
                longitude,
                accuracy,
                clientTimestamp,
                serverTimestamp,
                label);
    }

    // Record-like accessors for backward compatibility
    public String eventId() {
        return eventId;
    }

    public String userId() {
        return userId;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public double accuracy() {
        return accuracy;
    }

    public Instant clientTimestamp() {
        return clientTimestamp;
    }

    public Instant serverTimestamp() {
        return serverTimestamp;
    }

    public String label() {
        return label;
    }
}
