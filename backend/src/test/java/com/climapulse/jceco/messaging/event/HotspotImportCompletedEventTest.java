package com.climapulse.jceco.messaging.event;

import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HotspotImportCompletedEventTest {

    @Test
    void shouldBuildEventFromImportSummary() {
        var summary = new InpeHotspotImportSummary(3, 2, 0, 1, 120);

        var event = HotspotImportCompletedEvent.from(summary, Instant.parse("2026-09-08T14:37:00Z"));

        assertThat(event.eventType()).isEqualTo(DomainEventType.HOTSPOT_IMPORT_COMPLETED);
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-09-08T14:37:00Z"));
        assertThat(event.filesFound()).isEqualTo(3);
        assertThat(event.filesImported()).isEqualTo(2);
        assertThat(event.filesSkipped()).isZero();
        assertThat(event.failedFiles()).isEqualTo(1);
        assertThat(event.hotspotsSaved()).isEqualTo(120);
    }

    @Test
    void shouldReadEventFromPayload() {
        var payload = """
                {"eventType":"HOTSPOT_IMPORT_COMPLETED","occurredAt":"2026-09-08T14:37:00Z","filesFound":3,"filesImported":2,"filesSkipped":0,"failedFiles":1,"hotspotsSaved":120}
                """;

        var event = HotspotImportCompletedEvent.fromPayload(payload);

        assertThat(event).isPresent();
        assertThat(event.get().hotspotsSaved()).isEqualTo(120);
        assertThat(event.get().occurredAt()).isEqualTo(Instant.parse("2026-09-08T14:37:00Z"));
    }

    @Test
    void shouldReturnEmptyWhenPayloadCannotBeRead() {
        var event = HotspotImportCompletedEvent.fromPayload("invalid-payload");

        assertThat(event).isEmpty();
    }
}
