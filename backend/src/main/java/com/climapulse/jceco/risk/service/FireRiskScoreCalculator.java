package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.hotspot.service.HotspotQueryService;
import com.climapulse.jceco.risk.model.FireRiskAssessment;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.weather.persistence.WeatherSnapshotEntity;
import com.climapulse.jceco.weather.service.WeatherSnapshotService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class FireRiskScoreCalculator {

    private static final double HOTSPOT_WEIGHT = 0.40;
    private static final double HUMIDITY_WEIGHT = 0.25;
    private static final double DRY_DAYS_WEIGHT = 0.20;
    private static final double NO_RAIN_FORECAST_WEIGHT = 0.15;
    private static final long HOTSPOT_ANALYSIS_HOURS = 24;

    private final HotspotQueryService hotspotQueryService;
    private final WeatherSnapshotService weatherSnapshotService;
    private final Clock clock;

    public FireRiskScoreCalculator(
            HotspotQueryService hotspotQueryService,
            WeatherSnapshotService weatherSnapshotService
    ) {
        this(hotspotQueryService, weatherSnapshotService, Clock.systemUTC());
    }

    FireRiskScoreCalculator(
            HotspotQueryService hotspotQueryService,
            WeatherSnapshotService weatherSnapshotService,
            Clock clock
    ) {
        this.hotspotQueryService = hotspotQueryService;
        this.weatherSnapshotService = weatherSnapshotService;
        this.clock = clock;
    }

    public FireRiskAssessment calculate(double latitude, double longitude, double radiusMeters) {
        Instant calculatedAt = clock.instant();
        Instant observedSince = calculatedAt.minus(HOTSPOT_ANALYSIS_HOURS, ChronoUnit.HOURS);

        long recentHotspotCount = hotspotQueryService.countWithinRadiusSince(
                latitude,
                longitude,
                radiusMeters,
                observedSince
        );

        WeatherSnapshotEntity weatherSnapshot = weatherSnapshotService.getCurrentSnapshot(latitude, longitude);

        double hotspotFactor = hotspotFactor(recentHotspotCount);
        Double humidityFactor = humidityFactor(weatherSnapshot.getRelativeHumidity());
        Double dryDaysFactor = null;
        Double noRainForecastFactor = noRainForecastFactor(
                weatherSnapshot.getPrecipitationMm(),
                weatherSnapshot.getPrecipitationProbability()
        );

        int score = weightedScore(hotspotFactor, humidityFactor, dryDaysFactor, noRainForecastFactor);

        return new FireRiskAssessment(
                latitude,
                longitude,
                radiusMeters,
                recentHotspotCount,
                score,
                level(score),
                hotspotFactor,
                humidityFactor,
                dryDaysFactor,
                noRainForecastFactor,
                calculatedAt
        );
    }

    private double hotspotFactor(long recentHotspotCount) {
        return Math.min(100, recentHotspotCount * 10.0);
    }

    private Double humidityFactor(Double relativeHumidity) {
        if (relativeHumidity == null) {
            return null;
        }

        if (relativeHumidity <= 20) {
            return 100.0;
        }

        if (relativeHumidity >= 60) {
            return 0.0;
        }

        return (60 - relativeHumidity) * 2.5;
    }

    private Double noRainForecastFactor(Double precipitationMm, Double precipitationProbability) {
        if (precipitationMm != null && precipitationMm > 0) {
            return 0.0;
        }

        if (precipitationProbability == null) {
            return null;
        }

        return Math.max(0, Math.min(100, 100 - precipitationProbability));
    }

    private int weightedScore(
            double hotspotFactor,
            Double humidityFactor,
            Double dryDaysFactor,
            Double noRainForecastFactor
    ) {
        double weightedSum = hotspotFactor * HOTSPOT_WEIGHT;
        double usedWeight = HOTSPOT_WEIGHT;

        if (humidityFactor != null) {
            weightedSum += humidityFactor * HUMIDITY_WEIGHT;
            usedWeight += HUMIDITY_WEIGHT;
        }

        if (dryDaysFactor != null) {
            weightedSum += dryDaysFactor * DRY_DAYS_WEIGHT;
            usedWeight += DRY_DAYS_WEIGHT;
        }

        if (noRainForecastFactor != null) {
            weightedSum += noRainForecastFactor * NO_RAIN_FORECAST_WEIGHT;
            usedWeight += NO_RAIN_FORECAST_WEIGHT;
        }

        return (int) Math.round(weightedSum / usedWeight);
    }

    private RiskLevel level(int score) {
        if (score >= 80) {
            return RiskLevel.CRITICAL;
        }

        if (score >= 60) {
            return RiskLevel.HIGH;
        }

        if (score >= 40) {
            return RiskLevel.MODERATE;
        }

        return RiskLevel.LOW;
    }
}
