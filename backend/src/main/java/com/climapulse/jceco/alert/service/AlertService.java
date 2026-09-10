package com.climapulse.jceco.alert.service;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.alert.persistence.AlertRepository;
import com.climapulse.jceco.messaging.producer.AlertCreatedEventProducer;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertCreatedEventProducer eventProducer;
    private final Clock clock;

    @Autowired
    public AlertService(
            AlertRepository alertRepository,
            AlertCreatedEventProducer eventProducer
    ) {
        this(alertRepository, eventProducer, Clock.systemUTC());
    }

    AlertService(
            AlertRepository alertRepository,
            AlertCreatedEventProducer eventProducer,
            Clock clock
    ) {
        this.alertRepository = alertRepository;
        this.eventProducer = eventProducer;
        this.clock = clock;
    }

    @Transactional
    public Optional<AlertEntity> createOpenAlertIfNeeded(RiskAssessmentEntity assessment) {
        AlertSeverity severity = severityFor(assessment.getLevel());

        if (severity == null) {
            return Optional.empty();
        }

        var existingAlert = alertRepository
                .findFirstByLatitudeAndLongitudeAndRadiusMetersAndStatusOrderByCreatedAtDesc(
                        assessment.getLatitude(),
                        assessment.getLongitude(),
                        assessment.getRadiusMeters(),
                        AlertStatus.OPEN
                );

        if (existingAlert.isPresent()) {
            return existingAlert;
        }

        AlertEntity alert = alertRepository.save(AlertEntity.open(assessment, severity, clock.instant()));
        eventProducer.publish(alert);

        return Optional.of(alert);
    }

    private AlertSeverity severityFor(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CRITICAL -> AlertSeverity.CRITICAL;
            case HIGH -> AlertSeverity.HIGH;
            case LOW, MODERATE -> null;
        };
    }
}
