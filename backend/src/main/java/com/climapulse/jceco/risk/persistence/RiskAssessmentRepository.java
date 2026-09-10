package com.climapulse.jceco.risk.persistence;

import com.climapulse.jceco.risk.model.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {

    Page<RiskAssessmentEntity> findAllByOrderByCalculatedAtDesc(Pageable pageable);

    Page<RiskAssessmentEntity> findByLevelOrderByCalculatedAtDesc(
            RiskLevel level,
            Pageable pageable
    );

    Page<RiskAssessmentEntity> findByLatitudeAndLongitudeAndRadiusMetersOrderByCalculatedAtDesc(
            double latitude,
            double longitude,
            double radiusMeters,
            Pageable pageable
    );

    Page<RiskAssessmentEntity> findByCalculatedAtBetweenOrderByCalculatedAtDesc(
            Instant from,
            Instant to,
            Pageable pageable
    );
}
