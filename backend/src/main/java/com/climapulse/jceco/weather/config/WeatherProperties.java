package com.climapulse.jceco.weather.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "climapulse.weather")
public record WeatherProperties(
        @Positive int coordinateScale,
        @NotNull Duration snapshotTtl
) {
}
