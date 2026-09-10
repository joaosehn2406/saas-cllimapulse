package com.climapulse.jceco.messaging.event;

import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

public record HotspotImportCompletedEvent(
        DomainEventType eventType,
        Instant occurredAt,
        int filesFound,
        int filesImported,
        int filesSkipped,
        int failedFiles,
        int hotspotsSaved
) {

    public static HotspotImportCompletedEvent from(InpeHotspotImportSummary summary, Instant occurredAt) {
        return new HotspotImportCompletedEvent(
                DomainEventType.HOTSPOT_IMPORT_COMPLETED,
                occurredAt,
                summary.filesFound(),
                summary.filesImported(),
                summary.filesSkipped(),
                summary.failedFiles(),
                summary.hotspotsSaved()
        );
    }

    public static Optional<HotspotImportCompletedEvent> fromPayload(String payload) {
        try {
            return Optional.of(new HotspotImportCompletedEvent(
                    DomainEventType.valueOf(textValue(payload, "eventType")),
                    Instant.parse(textValue(payload, "occurredAt")),
                    intValue(payload, "filesFound"),
                    intValue(payload, "filesImported"),
                    intValue(payload, "filesSkipped"),
                    intValue(payload, "failedFiles"),
                    intValue(payload, "hotspotsSaved")
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public String toPayload() {
        return """
                {"eventType":"%s","occurredAt":"%s","filesFound":%d,"filesImported":%d,"filesSkipped":%d,"failedFiles":%d,"hotspotsSaved":%d}
                """.formatted(
                eventType.name(),
                occurredAt,
                filesFound,
                filesImported,
                filesSkipped,
                failedFiles,
                hotspotsSaved
        ).trim();
    }

    private static String textValue(String payload, String fieldName) {
        var matcher = Pattern.compile("\"%s\":\"([^\"]+)\"".formatted(fieldName)).matcher(payload);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + fieldName);
        }

        return matcher.group(1);
    }

    private static int intValue(String payload, String fieldName) {
        var matcher = Pattern.compile("\"%s\":(\\d+)".formatted(fieldName)).matcher(payload);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + fieldName);
        }

        return Integer.parseInt(matcher.group(1));
    }
}
