package com.climapulse.jceco.alert.persistence;

import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.alert.model.AlertSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {

    Optional<AlertEntity> findFirstByLatitudeAndLongitudeAndRadiusMetersAndStatusOrderByCreatedAtDesc(
            double latitude,
            double longitude,
            double radiusMeters,
            AlertStatus status
    );

    Page<AlertEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AlertEntity> findByStatusOrderByCreatedAtDesc(AlertStatus status, Pageable pageable);

    Page<AlertEntity> findBySeverityOrderByCreatedAtDesc(AlertSeverity severity, Pageable pageable);
}
