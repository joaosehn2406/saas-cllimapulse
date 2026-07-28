package com.climapulse.jceco.integration.inpe.model;

import com.climapulse.jceco.shared.exception.InvalidCoordinateException;

import java.time.Instant;

public record InpeHotspot(
        double latitude,
        double longitude,
        String satellite,
        Instant observedAt
) {
    public InpeHotspot {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new InvalidCoordinateException(latitude, longitude);
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException(latitude, longitude);
        }
    }
}