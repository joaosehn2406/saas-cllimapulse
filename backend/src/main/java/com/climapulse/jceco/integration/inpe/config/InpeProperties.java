package com.climapulse.jceco.integration.inpe.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "climapulse.inpe")
public record InpeProperties(
        @NotNull URI csvBaseUrl,
        @Min(1) int recentFilesCount
) {
}