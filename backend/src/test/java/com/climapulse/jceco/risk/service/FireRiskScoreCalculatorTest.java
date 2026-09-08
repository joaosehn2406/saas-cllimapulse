package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.hotspot.service.HotspotQueryService;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.weather.persistence.WeatherSnapshotEntity;
import com.climapulse.jceco.weather.service.WeatherSnapshotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FireRiskScoreCalculatorTest {

    @Mock
    private HotspotQueryService hotspotQueryService;

    @Mock
    private WeatherSnapshotService weatherSnapshotService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-08T14:37:00Z"), ZoneOffset.UTC);

    @Test
    void shouldCalculateHighRiskWhenHotspotsAreRecentAndWeatherIsDry() {
        var calculator = new FireRiskScoreCalculator(hotspotQueryService, weatherSnapshotService, clock);

        when(hotspotQueryService.countWithinRadiusSince(
                -26.9189,
                -49.0661,
                10_000,
                Instant.parse("2026-09-07T14:37:00Z")
        )).thenReturn(8L);
        when(weatherSnapshotService.getCurrentSnapshot(-26.9189, -49.0661))
                .thenReturn(snapshot(25.0, 0.0, 5.0));

        var assessment = calculator.calculate(-26.9189, -49.0661, 10_000);

        assertThat(assessment.recentHotspotCount()).isEqualTo(8);
        assertThat(assessment.hotspotFactor()).isEqualTo(80.0);
        assertThat(assessment.humidityFactor()).isEqualTo(87.5);
        assertThat(assessment.dryDaysFactor()).isNull();
        assertThat(assessment.noRainForecastFactor()).isEqualTo(95.0);
        assertThat(assessment.score()).isEqualTo(85);
        assertThat(assessment.level()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(assessment.calculatedAt()).isEqualTo(Instant.parse("2026-09-08T14:37:00Z"));
    }

    @Test
    void shouldCalculateLowRiskWhenThereAreFewHotspotsAndHumidityIsHigh() {
        var calculator = new FireRiskScoreCalculator(hotspotQueryService, weatherSnapshotService, clock);

        when(hotspotQueryService.countWithinRadiusSince(
                -26.9189,
                -49.0661,
                10_000,
                Instant.parse("2026-09-07T14:37:00Z")
        )).thenReturn(1L);
        when(weatherSnapshotService.getCurrentSnapshot(-26.9189, -49.0661))
                .thenReturn(snapshot(70.0, 2.0, 90.0));

        var assessment = calculator.calculate(-26.9189, -49.0661, 10_000);

        assertThat(assessment.score()).isEqualTo(5);
        assertThat(assessment.level()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void shouldIgnoreMissingWeatherFactorsInsteadOfTreatingThemAsZero() {
        var calculator = new FireRiskScoreCalculator(hotspotQueryService, weatherSnapshotService, clock);

        when(hotspotQueryService.countWithinRadiusSince(
                -26.9189,
                -49.0661,
                10_000,
                Instant.parse("2026-09-07T14:37:00Z")
        )).thenReturn(3L);
        when(weatherSnapshotService.getCurrentSnapshot(-26.9189, -49.0661))
                .thenReturn(snapshot(null, null, null));

        var assessment = calculator.calculate(-26.9189, -49.0661, 10_000);

        assertThat(assessment.humidityFactor()).isNull();
        assertThat(assessment.dryDaysFactor()).isNull();
        assertThat(assessment.noRainForecastFactor()).isNull();
        assertThat(assessment.score()).isEqualTo(30);
        assertThat(assessment.level()).isEqualTo(RiskLevel.LOW);
    }

    private WeatherSnapshotEntity snapshot(
            Double relativeHumidity,
            Double precipitationMm,
            Double precipitationProbability
    ) {
        return new WeatherSnapshotEntity(
                -26.92,
                -49.07,
                Instant.parse("2026-09-08T14:00:00Z"),
                Instant.parse("2026-09-08T14:37:00Z"),
                25.0,
                relativeHumidity,
                precipitationMm,
                precipitationProbability,
                12.0,
                19.0,
                1.2,
                0.20,
                "OPEN_METEO"
        );
    }
}
