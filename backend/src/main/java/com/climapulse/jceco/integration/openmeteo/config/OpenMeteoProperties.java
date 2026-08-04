package com.climapulse.jceco.integration.openmeteo.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "climapulse.open-meteo")
public record OpenMeteoProperties(
        @NotNull URI baseUrl,
        @Positive int forecastHours,
        @PositiveOrZero int pastHours
) {
}
