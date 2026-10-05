package com.climapulse.jceco.weather.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecastEntity, UUID> {

    Optional<WeatherForecastEntity> findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
            double latitude,
            double longitude,
            Instant forecastTime,
            Instant collectedAt,
            String source
    );
}
