package com.climapulse.jceco.risk.model;

import java.time.Instant;

public record FireRiskAssessment(
        double latitude,
        double longitude,
        double radiusMeters,
        long recentHotspotCount,
        int score,
        RiskLevel level,
        double hotspotFactor,
        Double humidityFactor,
        Double dryDaysFactor,
        Double noRainForecastFactor,
        Instant calculatedAt
) {
}
