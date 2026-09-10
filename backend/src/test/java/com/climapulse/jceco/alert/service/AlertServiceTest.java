package com.climapulse.jceco.alert.service;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.alert.persistence.AlertRepository;
import com.climapulse.jceco.messaging.producer.AlertCreatedEventProducer;
import com.climapulse.jceco.risk.model.FireRiskAssessment;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertCreatedEventProducer eventProducer;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-09T05:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldCreateCriticalAlertWhenAssessmentIsCritical() {
        var service = new AlertService(alertRepository, eventProducer, clock);
        var assessment = assessment(RiskLevel.CRITICAL, 85);
        var savedAlert = AlertEntity.open(assessment, AlertSeverity.CRITICAL, Instant.parse("2026-09-09T05:00:00Z"));

        when(alertRepository.findFirstByLatitudeAndLongitudeAndRadiusMetersAndStatusOrderByCreatedAtDesc(
                -26.9189,
                -49.0661,
                10_000,
                AlertStatus.OPEN
        )).thenReturn(Optional.empty());
        when(alertRepository.save(org.mockito.ArgumentMatchers.any(AlertEntity.class))).thenReturn(savedAlert);

        var response = service.createOpenAlertIfNeeded(assessment);

        assertThat(response).containsSame(savedAlert);

        var captor = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(captor.capture());

        var alert = captor.getValue();
        assertThat(alert.getRiskAssessmentId()).isEqualTo(assessment.getId());
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getCreatedAt()).isEqualTo(Instant.parse("2026-09-09T05:00:00Z"));
        verify(eventProducer).publish(savedAlert);
    }

    @Test
    void shouldReturnExistingOpenAlertInsteadOfCreatingDuplicate() {
        var service = new AlertService(alertRepository, eventProducer, clock);
        var assessment = assessment(RiskLevel.HIGH, 70);
        var existingAlert = AlertEntity.open(assessment, AlertSeverity.HIGH, Instant.parse("2026-09-09T04:00:00Z"));

        when(alertRepository.findFirstByLatitudeAndLongitudeAndRadiusMetersAndStatusOrderByCreatedAtDesc(
                -26.9189,
                -49.0661,
                10_000,
                AlertStatus.OPEN
        )).thenReturn(Optional.of(existingAlert));

        var response = service.createOpenAlertIfNeeded(assessment);

        assertThat(response).containsSame(existingAlert);
        verifyNoMoreInteractions(alertRepository);
        verifyNoInteractions(eventProducer);
    }

    @Test
    void shouldNotCreateAlertForModerateRisk() {
        var service = new AlertService(alertRepository, eventProducer, clock);
        var assessment = assessment(RiskLevel.MODERATE, 45);

        var response = service.createOpenAlertIfNeeded(assessment);

        assertThat(response).isEmpty();
        verifyNoMoreInteractions(alertRepository);
        verifyNoInteractions(eventProducer);
    }

    private RiskAssessmentEntity assessment(RiskLevel level, int score) {
        var assessment = new FireRiskAssessment(
                -26.9189,
                -49.0661,
                10_000,
                5,
                score,
                level,
                50.0,
                70.0,
                null,
                90.0,
                Instant.parse("2026-09-09T05:00:00Z")
        );
        var entity = RiskAssessmentEntity.from(
                assessment,
                Instant.parse("2026-09-09T06:00:00Z"),
                "v1"
        );

        ReflectionTestUtils.setField(entity, "id", UUID.fromString("0199187d-81a4-7d7a-9e66-df2f45e1d88a"));

        return entity;
    }
}
