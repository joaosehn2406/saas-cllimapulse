package com.climapulse.jceco.messaging.producer;

import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;
import com.climapulse.jceco.messaging.config.ClimapulseKafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class HotspotImportEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotspotImportEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ClimapulseKafkaProperties properties;
    private final Clock clock;

    @Autowired
    public HotspotImportEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ClimapulseKafkaProperties properties
    ) {
        this(kafkaTemplate, properties, Clock.systemUTC());
    }

    HotspotImportEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ClimapulseKafkaProperties properties,
            Clock clock
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    public void publish(InpeHotspotImportSummary summary) {
        if (summary.filesFound() == 0) {
            return;
        }

        String occurredAt = clock.instant().toString();
        String payload = """
                {"eventType":"HOTSPOT_IMPORT_COMPLETED","occurredAt":"%s","filesFound":%d,"filesImported":%d,"filesSkipped":%d,"failedFiles":%d,"hotspotsSaved":%d}
                """.formatted(
                occurredAt,
                summary.filesFound(),
                summary.filesImported(),
                summary.filesSkipped(),
                summary.failedFiles(),
                summary.hotspotsSaved()
        ).trim();

        try {
            var future = kafkaTemplate.send(properties.hotspotImportCompletedTopic(), occurredAt, payload);

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    LOGGER.warn("Could not publish hotspot import event", exception);
                }
            });
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not publish hotspot import event", exception);
        }
    }
}
