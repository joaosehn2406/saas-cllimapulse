package com.climapulse.jceco.risk.persistence;

import com.climapulse.jceco.risk.model.FireRiskAssessment;
import com.climapulse.jceco.risk.model.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessment", schema = "climapulse")
public class RiskAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "radius_meters", nullable = false)
    private double radiusMeters;

    @Column(name = "recent_hotspot_count", nullable = false)
    private long recentHotspotCount;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel level;

    @Column(name = "hotspot_factor", nullable = false)
    private double hotspotFactor;

    @Column(name = "humidity_factor")
    private Double humidityFactor;

    @Column(name = "dry_days_factor")
    private Double dryDaysFactor;

    @Column(name = "no_rain_forecast_factor")
    private Double noRainForecastFactor;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "methodology_version", nullable = false, length = 20)
    private String methodologyVersion;

    protected RiskAssessmentEntity() {
    }

    public RiskAssessmentEntity(
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
            Instant calculatedAt,
            Instant validUntil,
            String methodologyVersion
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.recentHotspotCount = recentHotspotCount;
        this.score = score;
        this.level = level;
        this.hotspotFactor = hotspotFactor;
        this.humidityFactor = humidityFactor;
        this.dryDaysFactor = dryDaysFactor;
        this.noRainForecastFactor = noRainForecastFactor;
        this.calculatedAt = calculatedAt;
        this.validUntil = validUntil;
        this.methodologyVersion = methodologyVersion;
    }

    public static RiskAssessmentEntity from(
            FireRiskAssessment assessment,
            Instant validUntil,
            String methodologyVersion
    ) {
        return new RiskAssessmentEntity(
                assessment.latitude(),
                assessment.longitude(),
                assessment.radiusMeters(),
                assessment.recentHotspotCount(),
                assessment.score(),
                assessment.level(),
                assessment.hotspotFactor(),
                assessment.humidityFactor(),
                assessment.dryDaysFactor(),
                assessment.noRainForecastFactor(),
                assessment.calculatedAt(),
                validUntil,
                methodologyVersion
        );
    }

    public UUID getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadiusMeters() {
        return radiusMeters;
    }

    public long getRecentHotspotCount() {
        return recentHotspotCount;
    }

    public int getScore() {
        return score;
    }

    public RiskLevel getLevel() {
        return level;
    }

    public double getHotspotFactor() {
        return hotspotFactor;
    }

    public Double getHumidityFactor() {
        return humidityFactor;
    }

    public Double getDryDaysFactor() {
        return dryDaysFactor;
    }

    public Double getNoRainForecastFactor() {
        return noRainForecastFactor;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public String getMethodologyVersion() {
        return methodologyVersion;
    }
}
