package com.climapulse.jceco.messaging.producer;

import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;
import com.climapulse.jceco.messaging.config.ClimapulseKafkaProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HotspotImportEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ClimapulseKafkaProperties properties = new ClimapulseKafkaProperties(
            "climate.hotspot.imported.v1",
            "climate.alert.created.v1"
    );
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-08T14:37:00Z"), ZoneOffset.UTC);

    @Test
    void shouldPublishImportCompletedEventWhenFilesWereFound() {
        var producer = new HotspotImportEventProducer(kafkaTemplate, properties, clock);
        var summary = new InpeHotspotImportSummary(3, 2, 0, 1, 120);

        producer.publish(summary);

        verify(kafkaTemplate).send(
                "climate.hotspot.imported.v1",
                "2026-09-08T14:37:00Z",
                """
                        {"eventType":"HOTSPOT_IMPORT_COMPLETED","occurredAt":"2026-09-08T14:37:00Z","filesFound":3,"filesImported":2,"filesSkipped":0,"failedFiles":1,"hotspotsSaved":120}
                        """.trim()
        );
    }

    @Test
    void shouldNotPublishEventWhenNoFilesWereFound() {
        var producer = new HotspotImportEventProducer(kafkaTemplate, properties, clock);
        var summary = new InpeHotspotImportSummary(0, 0, 0, 0, 0);

        producer.publish(summary);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldNotThrowWhenKafkaPublishFails() {
        var producer = new HotspotImportEventProducer(kafkaTemplate, properties, clock);
        var summary = new InpeHotspotImportSummary(1, 1, 0, 0, 10);

        doThrow(new RuntimeException("Kafka unavailable"))
                .when(kafkaTemplate)
                .send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        producer.publish(summary);

        verify(kafkaTemplate).send(
                org.mockito.ArgumentMatchers.eq("climate.hotspot.imported.v1"),
                org.mockito.ArgumentMatchers.eq("2026-09-08T14:37:00Z"),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
