package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.risk.model.FireRiskAssessment;
import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.persistence.RiskAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private FireRiskScoreCalculator fireRiskScoreCalculator;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Test
    void shouldCalculateAndSaveRiskAssessment() {
        var service = new RiskAssessmentService(fireRiskScoreCalculator, riskAssessmentRepository);
        var assessment = new FireRiskAssessment(
                -26.9189,
                -49.0661,
                10_000,
                8,
                85,
                RiskLevel.CRITICAL,
                80.0,
                87.5,
                null,
                95.0,
                Instant.parse("2026-09-08T14:37:00Z")
        );
        var savedEntity = RiskAssessmentEntity.from(
                assessment,
                Instant.parse("2026-09-08T15:37:00Z"),
                "v1"
        );

        when(fireRiskScoreCalculator.calculate(-26.9189, -49.0661, 10_000))
                .thenReturn(assessment);
        when(riskAssessmentRepository.save(org.mockito.ArgumentMatchers.any(RiskAssessmentEntity.class)))
                .thenReturn(savedEntity);

        var response = service.calculateAndSave(-26.9189, -49.0661, 10_000);

        assertThat(response).isSameAs(savedEntity);

        var captor = ArgumentCaptor.forClass(RiskAssessmentEntity.class);
        verify(riskAssessmentRepository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getLatitude()).isEqualTo(-26.9189);
        assertThat(entity.getLongitude()).isEqualTo(-49.0661);
        assertThat(entity.getRadiusMeters()).isEqualTo(10_000);
        assertThat(entity.getRecentHotspotCount()).isEqualTo(8);
        assertThat(entity.getScore()).isEqualTo(85);
        assertThat(entity.getLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(entity.getValidUntil()).isEqualTo(Instant.parse("2026-09-08T15:37:00Z"));
        assertThat(entity.getMethodologyVersion()).isEqualTo("v1");
    }
}
