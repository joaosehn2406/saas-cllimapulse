package com.climapulse.jceco.weather.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshotEntity, UUID> {

    Optional<WeatherSnapshotEntity> findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
            double latitude,
            double longitude,
            Instant forecastTime,
            Instant collectedAt,
            String source
    );
}
