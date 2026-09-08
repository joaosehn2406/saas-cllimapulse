package com.climapulse.jceco.risk.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {

    Page<RiskAssessmentEntity> findAllByOrderByCalculatedAtDesc(Pageable pageable);
}
