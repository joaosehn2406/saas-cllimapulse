package com.climapulse.jceco.alert.persistence;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
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
@Table(name = "alert", schema = "climapulse")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "risk_assessment_id", nullable = false)
    private UUID riskAssessmentId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "radius_meters", nullable = false)
    private double radiusMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected AlertEntity() {
    }

    public AlertEntity(
            UUID riskAssessmentId,
            double latitude,
            double longitude,
            double radiusMeters,
            AlertSeverity severity,
            AlertStatus status,
            String title,
            String message,
            Instant createdAt,
            Instant resolvedAt
    ) {
        this.riskAssessmentId = riskAssessmentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static AlertEntity open(
            RiskAssessmentEntity assessment,
            AlertSeverity severity,
            Instant createdAt
    ) {
        return new AlertEntity(
                assessment.getId(),
                assessment.getLatitude(),
                assessment.getLongitude(),
                assessment.getRadiusMeters(),
                severity,
                AlertStatus.OPEN,
                "%s fire risk detected".formatted(severity.name()),
                "Fire risk score %d detected within %.0f meters of latitude %.4f and longitude %.4f."
                        .formatted(
                                assessment.getScore(),
                                assessment.getRadiusMeters(),
                                assessment.getLatitude(),
                                assessment.getLongitude()
                        ),
                createdAt,
                null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getRiskAssessmentId() {
        return riskAssessmentId;
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

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
