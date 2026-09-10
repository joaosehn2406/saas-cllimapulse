package com.climapulse.jceco.messaging.producer;

import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.messaging.config.ClimapulseKafkaProperties;
import com.climapulse.jceco.messaging.event.AlertCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class AlertCreatedEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertCreatedEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ClimapulseKafkaProperties properties;
    private final Clock clock;

    @Autowired
    public AlertCreatedEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ClimapulseKafkaProperties properties
    ) {
        this(kafkaTemplate, properties, Clock.systemUTC());
    }

    AlertCreatedEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ClimapulseKafkaProperties properties,
            Clock clock
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    public void publish(AlertEntity alert) {
        var event = AlertCreatedEvent.from(alert, clock.instant());
        String key = alert.getId().toString();
        String payload = event.toPayload();

        try {
            var future = kafkaTemplate.send(properties.alertCreatedTopic(), key, payload);

            if (future != null) {
                future.whenComplete((result, exception) -> {
                    if (exception != null) {
                        LOGGER.warn("Could not publish alert created event", exception);
                    }
                });
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not publish alert created event", exception);
        }
    }
}
