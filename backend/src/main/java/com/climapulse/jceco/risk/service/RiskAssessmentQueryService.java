package com.climapulse.jceco.risk.service;

import com.climapulse.jceco.risk.model.RiskLevel;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.persistence.RiskAssessmentRepository;
import com.climapulse.jceco.shared.pagination.PageRequestFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RiskAssessmentQueryService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final PageRequestFactory pageRequestFactory;

    public RiskAssessmentQueryService(
            RiskAssessmentRepository riskAssessmentRepository,
            PageRequestFactory pageRequestFactory
    ) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.pageRequestFactory = pageRequestFactory;
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findRecent() {
        return findRecent(0);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findRecent(int page) {
        return riskAssessmentRepository.findAllByOrderByCalculatedAtDesc(pageRequestFactory.defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findByLevel(RiskLevel level, int page) {
        return riskAssessmentRepository.findByLevelOrderByCalculatedAtDesc(level, pageRequestFactory.defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findByArea(
            double latitude,
            double longitude,
            double radiusMeters,
            int page
    ) {
        return riskAssessmentRepository.findByLatitudeAndLongitudeAndRadiusMetersOrderByCalculatedAtDesc(
                latitude,
                longitude,
                radiusMeters,
                pageRequestFactory.defaultPage(page)
        );
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentEntity> findByPeriod(Instant from, Instant to, int page) {
        return riskAssessmentRepository.findByCalculatedAtBetweenOrderByCalculatedAtDesc(
                from,
                to,
                pageRequestFactory.defaultPage(page)
        );
    }
}
