package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.persistence.RiskAssessmentRepository;
import com.climapulse.jceco.shared.pagination.PageRequestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentQueryServiceTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    private final PageRequestFactory pageRequestFactory = new PageRequestFactory();

    @Test
    void shouldFindRecentAssessmentsUsingDefaultPageSize() {
        var service = new RiskAssessmentQueryService(riskAssessmentRepository, pageRequestFactory);
        var pageable = PageRequest.of(0, 20);
        var assessment = assessment(RiskLevel.CRITICAL);

        when(riskAssessmentRepository.findAllByOrderByCalculatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(assessment), pageable, 1));

        var page = service.findRecent();

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getContent().getFirst()).isSameAs(assessment);
    }

    @Test
    void shouldFindAssessmentsByLevel() {
        var service = new RiskAssessmentQueryService(riskAssessmentRepository, pageRequestFactory);
        var pageable = PageRequest.of(1, 20);
        var assessment = assessment(RiskLevel.HIGH);

        when(riskAssessmentRepository.findByLevelOrderByCalculatedAtDesc(RiskLevel.HIGH, pageable))
                .thenReturn(new PageImpl<>(List.of(assessment), pageable, 1));

        var page = service.findByLevel(RiskLevel.HIGH, 1);

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getContent().getFirst().getLevel()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void shouldFindAssessmentsByArea() {
        var service = new RiskAssessmentQueryService(riskAssessmentRepository, pageRequestFactory);
        var pageable = PageRequest.of(2, 20);

        when(riskAssessmentRepository.findByLatitudeAndLongitudeAndRadiusMetersOrderByCalculatedAtDesc(
                -26.9189,
                -49.0661,
                10_000,
                pageable
        )).thenReturn(new PageImpl<>(List.of(), pageable, 40));

        var page = service.findByArea(-26.9189, -49.0661, 10_000, 2);

        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void shouldFindAssessmentsByPeriod() {
        var service = new RiskAssessmentQueryService(riskAssessmentRepository, pageRequestFactory);
        var pageable = PageRequest.of(0, 20);
        var from = Instant.parse("2026-09-08T00:00:00Z");
        var to = Instant.parse("2026-09-09T00:00:00Z");

        when(riskAssessmentRepository.findByCalculatedAtBetweenOrderByCalculatedAtDesc(from, to, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 10));

        var page = service.findByPeriod(from, to, 0);

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
    }

    private RiskAssessmentEntity assessment(RiskLevel level) {
        return new RiskAssessmentEntity(
                -26.9189,
                -49.0661,
                10_000,
                8,
                85,
                level,
                80.0,
                87.5,
                null,
                95.0,
                Instant.parse("2026-09-08T14:37:00Z"),
                Instant.parse("2026-09-08T15:37:00Z"),
                "v1"
        );
    }
}
