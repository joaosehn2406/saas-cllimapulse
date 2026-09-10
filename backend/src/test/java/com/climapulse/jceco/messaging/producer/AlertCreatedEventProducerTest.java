package com.climapulse.jceco.messaging.producer;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.messaging.config.ClimapulseKafkaProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertCreatedEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ClimapulseKafkaProperties properties = new ClimapulseKafkaProperties(
            "climate.hotspot.imported.v1",
            "climate.alert.created.v1"
    );
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-08T14:37:00Z"), ZoneOffset.UTC);

    @Test
    void shouldPublishAlertCreatedEvent() {
        var producer = new AlertCreatedEventProducer(kafkaTemplate, properties, clock);
        var alert = alert();

        producer.publish(alert);

        verify(kafkaTemplate).send(
                "climate.alert.created.v1",
                "0199187d-81a4-7d7a-9e66-df2f45e1d88b",
                """
                        {"eventType":"ALERT_CREATED","occurredAt":"2026-09-08T14:37:00Z","alertId":"0199187d-81a4-7d7a-9e66-df2f45e1d88b","riskAssessmentId":"0199187d-81a4-7d7a-9e66-df2f45e1d88a","severity":"CRITICAL","latitude":-26.9189,"longitude":-49.0661,"radiusMeters":10000.0}
                        """.trim()
        );
    }

    @Test
    void shouldNotThrowWhenKafkaPublishFails() {
        var producer = new AlertCreatedEventProducer(kafkaTemplate, properties, clock);
        var alert = alert();

        doThrow(new RuntimeException("Kafka unavailable"))
                .when(kafkaTemplate)
                .send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        producer.publish(alert);

        verify(kafkaTemplate).send(
                org.mockito.ArgumentMatchers.eq("climate.alert.created.v1"),
                org.mockito.ArgumentMatchers.eq("0199187d-81a4-7d7a-9e66-df2f45e1d88b"),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    private AlertEntity alert() {
        var alert = new AlertEntity(
                UUID.fromString("0199187d-81a4-7d7a-9e66-df2f45e1d88a"),
                -26.9189,
                -49.0661,
                10_000,
                AlertSeverity.CRITICAL,
                com.climapulse.jceco.alert.model.AlertStatus.OPEN,
                "CRITICAL fire risk detected",
                "Fire risk score 85 detected within 10000 meters of latitude -26.9189 and longitude -49.0661.",
                Instant.parse("2026-09-08T14:30:00Z"),
                null
        );
        ReflectionTestUtils.setField(alert, "id", UUID.fromString("0199187d-81a4-7d7a-9e66-df2f45e1d88b"));

        return alert;
    }
}
