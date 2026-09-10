package com.climapulse.jceco.messaging.event;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.persistence.AlertEntity;

import java.time.Instant;
import java.util.UUID;

public record AlertCreatedEvent(
        DomainEventType eventType,
        Instant occurredAt,
        UUID alertId,
        UUID riskAssessmentId,
        AlertSeverity severity,
        double latitude,
        double longitude,
        double radiusMeters
) {

    public static AlertCreatedEvent from(AlertEntity alert, Instant occurredAt) {
        return new AlertCreatedEvent(
                DomainEventType.ALERT_CREATED,
                occurredAt,
                alert.getId(),
                alert.getRiskAssessmentId(),
                alert.getSeverity(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getRadiusMeters()
        );
    }

    public String toPayload() {
        return """
                {"eventType":"%s","occurredAt":"%s","alertId":"%s","riskAssessmentId":"%s","severity":"%s","latitude":%s,"longitude":%s,"radiusMeters":%s}
                """.formatted(
                eventType.name(),
                occurredAt,
                alertId,
                riskAssessmentId,
                severity.name(),
                latitude,
                longitude,
                radiusMeters
        ).trim();
    }
}
