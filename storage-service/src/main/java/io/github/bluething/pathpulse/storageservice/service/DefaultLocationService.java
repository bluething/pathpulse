package io.github.bluething.pathpulse.storageservice.service;

import io.github.bluething.pathpulse.storageservice.model.LocationEntity;
import io.github.bluething.pathpulse.storageservice.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class DefaultLocationService implements LocationService {
    private final LocationRepository repository;

    @Override
    public LocationEntity save(LocationEntity document) {
        log.debug("Saving location document: eventId={}, userId={}",
                document.eventId(), document.userId());
        return repository.save(document);
    }

    @Override
    public List<LocationEntity> getByUserId(String userId, int limit) {
        log.debug("Fetching locations for user: userId={}, limit={}", userId, limit);
        return repository.findByUserIdOrderByServerTimestampDesc(
                userId, PageRequest.of(0, limit));
    }

    @Override
    public List<LocationEntity> getByUserIdAndTimeRange(String userId, Instant start, Instant end) {
        log.debug("Fetching locations for user in time range: userId={}, start={}, end={}",
                userId, start, end);
        return repository.findByUserIdAndServerTimestampBetween(userId, start, end);
    }

    @Override
    public List<LocationEntity> getLatest(int limit) {
        log.debug("Fetching latest locations: limit={}", limit);
        return repository.findAllByOrderByServerTimestampDesc(PageRequest.of(0, limit));
    }

    @Override
    public long countByUserId(String userId) {
        return repository.countByUserId(userId);
    }

    @Override
    public long countAll() {
        return repository.count();
    }
}
