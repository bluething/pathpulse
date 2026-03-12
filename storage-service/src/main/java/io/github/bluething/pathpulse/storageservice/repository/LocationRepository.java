package io.github.bluething.pathpulse.storageservice.repository;

import io.github.bluething.pathpulse.storageservice.model.LocationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, String> {
    /**
     * Finds all locations for a specific user, ordered by server timestamp
     * descending.
     *
     * @param userId   The user ID to search for
     * @param pageable Pagination parameters
     * @return List of location documents
     */
    List<LocationEntity> findByUserIdOrderByServerTimestampDesc(String userId, Pageable pageable);

    /**
     * Finds all locations for a user within a time range.
     *
     * @param userId The user ID
     * @param start  Start of time range (inclusive)
     * @param end    End of time range (inclusive)
     * @return List of location documents
     */
    List<LocationEntity> findByUserIdAndServerTimestampBetween(
            String userId, Instant start, Instant end);

    /**
     * Finds the most recent locations across all users.
     *
     * @param pageable Pagination parameters
     * @return List of location documents
     */
    List<LocationEntity> findAllByOrderByServerTimestampDesc(Pageable pageable);

    /**
     * Counts total locations for a specific user.
     *
     * @param userId The user ID
     * @return Count of locations
     */
    long countByUserId(String userId);
}
