package com.climapulse.jceco.messaging.consumer;

import com.climapulse.jceco.alert.service.AlertService;
import com.climapulse.jceco.risk.config.RiskProperties;
import com.climapulse.jceco.risk.model.FireRiskAssessment;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.service.RiskAssessmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotspotImportCompletedConsumerTest {

    @Mock
    private RiskAssessmentService riskAssessmentService;

    @Mock
    private AlertService alertService;

    @Test
    void shouldCalculateRiskForConfiguredAreasWhenHotspotsWereSaved() {
        var properties = new RiskProperties(List.of(
                new RiskProperties.MonitoredArea("test-area", -26.9189, -49.0661, 10_000)
        ));
        var consumer = new HotspotImportCompletedConsumer(riskAssessmentService, alertService, properties);
        var assessment = assessment();

        when(riskAssessmentService.calculateAndSave(-26.9189, -49.0661, 10_000))
                .thenReturn(assessment);
        when(alertService.createOpenAlertIfNeeded(assessment)).thenReturn(Optional.empty());

        consumer.consume(importEventPayload(12));

        verify(riskAssessmentService).calculateAndSave(-26.9189, -49.0661, 10_000);
        verify(alertService).createOpenAlertIfNeeded(assessment);
    }

    @Test
    void shouldIgnoreEventWhenNoHotspotsWereSaved() {
        var properties = new RiskProperties(List.of(
                new RiskProperties.MonitoredArea("test-area", -26.9189, -49.0661, 10_000)
        ));
        var consumer = new HotspotImportCompletedConsumer(riskAssessmentService, alertService, properties);

        consumer.consume(importEventPayload(0));

        verifyNoInteractions(riskAssessmentService, alertService);
    }

    @Test
    void shouldIgnoreEventWhenPayloadIsNotReadable() {
        var properties = new RiskProperties(List.of(
                new RiskProperties.MonitoredArea("test-area", -26.9189, -49.0661, 10_000)
        ));
        var consumer = new HotspotImportCompletedConsumer(riskAssessmentService, alertService, properties);

        consumer.consume("invalid-payload");

        verifyNoInteractions(riskAssessmentService, alertService);
    }

    @Test
    void shouldContinueProcessingOtherAreasWhenOneAreaFails() {
        var properties = new RiskProperties(List.of(
                new RiskProperties.MonitoredArea("first-area", -26.9189, -49.0661, 10_000),
                new RiskProperties.MonitoredArea("second-area", -23.5505, -46.6333, 20_000)
        ));
        var consumer = new HotspotImportCompletedConsumer(riskAssessmentService, alertService, properties);
        var assessment = assessment();

        when(riskAssessmentService.calculateAndSave(-26.9189, -49.0661, 10_000))
                .thenThrow(new RuntimeException("Open-Meteo unavailable"));
        when(riskAssessmentService.calculateAndSave(-23.5505, -46.6333, 20_000))
                .thenReturn(assessment);
        when(alertService.createOpenAlertIfNeeded(assessment)).thenReturn(Optional.empty());

        consumer.consume(importEventPayload(12));

        verify(riskAssessmentService).calculateAndSave(-26.9189, -49.0661, 10_000);
        verify(riskAssessmentService).calculateAndSave(-23.5505, -46.6333, 20_000);
        verify(alertService).createOpenAlertIfNeeded(assessment);
    }

    private RiskAssessmentEntity assessment() {
        var assessment = new FireRiskAssessment(
                -26.9189,
                -49.0661,
                10_000,
                5,
                85,
                RiskLevel.CRITICAL,
                50.0,
                70.0,
                null,
                90.0,
                Instant.parse("2026-09-09T05:00:00Z")
        );

        return RiskAssessmentEntity.from(
                assessment,
                Instant.parse("2026-09-09T06:00:00Z"),
                "v1"
        );
    }

    private String importEventPayload(int hotspotsSaved) {
        return """
                {"eventType":"HOTSPOT_IMPORT_COMPLETED","occurredAt":"2026-09-09T05:00:00Z","filesFound":1,"filesImported":1,"filesSkipped":0,"failedFiles":0,"hotspotsSaved":%d}
                """.formatted(hotspotsSaved).trim();
    }
}
