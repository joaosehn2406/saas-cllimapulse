package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.persistence.RiskAssessmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class RiskAssessmentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Duration ASSESSMENT_VALIDITY = Duration.ofHours(1);
    private static final String METHODOLOGY_VERSION = "v1";

    private final FireRiskScoreCalculator fireRiskScoreCalculator;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessmentService(
            FireRiskScoreCalculator fireRiskScoreCalculator,
            RiskAssessmentRepository riskAssessmentRepository
    ) {
        this.fireRiskScoreCalculator = fireRiskScoreCalculator;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    @Transactional
    public RiskAssessmentEntity calculateAndSave(double latitude, double longitude, double radiusMeters) {
        var assessment = fireRiskScoreCalculator.calculate(latitude, longitude, radiusMeters);
        var entity = RiskAssessmentEntity.from(
                assessment,
                assessment.calculatedAt().plus(ASSESSMENT_VALIDITY),
                METHODOLOGY_VERSION
        );

        return riskAssessmentRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findRecent() {
        return findRecent(0);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findRecent(int page) {
        return riskAssessmentRepository.findAllByOrderByCalculatedAtDesc(PageRequest.of(page, DEFAULT_PAGE_SIZE));
    }
}
