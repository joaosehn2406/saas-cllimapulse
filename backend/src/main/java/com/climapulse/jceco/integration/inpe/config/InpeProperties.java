package com.climapulse.jceco.integration.inpe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "climapulse.inpe")
public record InpeProperties(
        URI csvBaseUrl
) {
}