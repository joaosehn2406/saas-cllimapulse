package com.climapulse.jceco.messaging.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "climapulse.kafka")
public record ClimapulseKafkaProperties(
        @NotBlank String hotspotImportCompletedTopic,
        @NotBlank String alertCreatedTopic
) {
}
