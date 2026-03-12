package io.github.bluething.pathpulse.storageservice.service;

import io.github.bluething.pathpulse.storageservice.model.LocationEntity;

import java.time.Instant;
import java.util.List;

public interface LocationService {
    /**
     * Saves a location document.
     *
     * @param document The document to save
     * @return The saved document
     */
    LocationEntity save(LocationEntity document);

    /**
     * Gets recent locations for a user.
     *
     * @param userId The user ID
     * @param limit  Maximum number of results
     * @return List of location documents
     */
    List<LocationEntity> getByUserId(String userId, int limit);

    /**
     * Gets locations for a user within a time range.
     *
     * @param userId The user ID
     * @param start  Start of time range
     * @param end    End of time range
     * @return List of location documents
     */
    List<LocationEntity> getByUserIdAndTimeRange(String userId, Instant start, Instant end);

    /**
     * Gets the most recent locations across all users.
     *
     * @param limit Maximum number of results
     * @return List of location documents
     */
    List<LocationEntity> getLatest(int limit);

    /**
     * Gets the total count of locations for a user.
     *
     * @param userId The user ID
     * @return Count of locations
     */
    long countByUserId(String userId);

    /**
     * Gets the total count of all locations.
     *
     * @return Total count
     */
    long countAll();
}
