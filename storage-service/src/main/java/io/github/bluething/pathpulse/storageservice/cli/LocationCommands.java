package io.github.bluething.pathpulse.storageservice.cli;

import io.github.bluething.pathpulse.storageservice.model.LocationEntity;
import io.github.bluething.pathpulse.storageservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationCommands {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocationService locationService;

    @Command(name = "find-by-user", description = "Find locations by user ID")
    public String findByUser(
            @Option(longName = "user-id", required = true, description = "User ID to search for")
            String userId,

            @Option(longName = "limit", defaultValue = "10", description = "Maximum results")
            int limit
    ) {

        List<LocationEntity> locations = locationService.getByUserId(userId, limit);

        if (locations.isEmpty()) {
            return "No locations found for user: " + userId;
        }

        return formatLocations(locations, "Locations for user: " + userId);
    }

    @Command(description = "Get the latest locations across all users", name = "get-latest")
    public String getLatest(
            @Option(defaultValue = "10", description = "Maximum results") int limit) {

        List<LocationEntity> locations = locationService.getLatest(limit);

        if (locations.isEmpty()) {
            return "No locations found";
        }

        return formatLocations(locations, "Latest " + limit + " locations");
    }

    @Command(value = "Get location count for a user", name = "count-by-user")
    public String countByUser(
            @Option(description = "User ID to count") String userId) {

        long count = locationService.countByUserId(userId);
        return String.format("User '%s' has %d location(s) recorded", userId, count);
    }

    @Command(value = "Get total location count", name = "count-all")
    public String countAll() {
        long count = locationService.countAll();
        return String.format("Total locations: %d", count);
    }

    @Command(value = "Show system statistics", name = "stats")
    public String stats() {
        long total = locationService.countAll();

        // Using text blocks (stable since Java 15)
        return """
                ╔═══════════════════════════════════════╗
                ║       Location Tracking Stats         ║
                ╠═══════════════════════════════════════╣
                ║  Total Locations: %,15d     ║
                ╚═══════════════════════════════════════╝
                """.formatted(total);
    }

    private String formatLocations(List<LocationEntity> locations, String header) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n").append(header).append("\n");
        sb.append("═".repeat(80)).append("\n");
        sb.append(String.format("%-20s %-12s %-12s %-8s %-17s %-10s%n",
                "User ID", "Latitude", "Longitude", "Accuracy", "Timestamp", "Label"));
        sb.append("─".repeat(80)).append("\n");

        for (LocationEntity loc : locations) {
            String formattedTime = formatTimestamp(loc.serverTimestamp());
            sb.append(String.format("%-20s %12.6f %12.6f %8.1f %-17s %-10s%n",
                    truncate(loc.userId(), 20),
                    loc.latitude(),
                    loc.longitude(),
                    loc.accuracy(),
                    formattedTime,
                    loc.label()));
        }

        sb.append("═".repeat(80)).append("\n");
        sb.append("Total: ").append(locations.size()).append(" location(s)");

        return sb.toString();
    }

    private String formatTimestamp(Instant timestamp) {
        return LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault())
                .format(FORMATTER);
    }
    private String truncate(String str, int maxLength) {
        if (str == null)
            return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
