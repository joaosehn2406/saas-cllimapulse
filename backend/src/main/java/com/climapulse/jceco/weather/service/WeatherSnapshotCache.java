package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.weather.config.WeatherProperties;
import com.climapulse.jceco.weather.persistence.WeatherSnapshotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Component
class WeatherSnapshotCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherSnapshotCache.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    WeatherSnapshotCache(
            StringRedisTemplate redisTemplate,
            WeatherProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.ttl = properties.snapshotTtl();
    }

    Optional<WeatherSnapshotEntity> get(double latitude, double longitude, Instant forecastTime) {
        String key = key(latitude, longitude, forecastTime);

        try {
            String cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                return Optional.empty();
            }

            return Optional.of(CacheEntry.parse(cachedValue).toEntity());
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not read weather snapshot from Redis: {}", key, exception);
            return Optional.empty();
        }
    }

    void put(WeatherSnapshotEntity snapshot) {
        String key = key(snapshot.getLatitude(), snapshot.getLongitude(), snapshot.getForecastTime());

        try {
            String value = CacheEntry.from(snapshot).serialize();
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not save weather snapshot in Redis: {}", key, exception);
        }
    }

    private String key(double latitude, double longitude, Instant forecastTime) {
        return String.format(Locale.US, "weather:point:%.2f:%.2f:%s", latitude, longitude, forecastTime);
    }

    private record CacheEntry(
            double latitude,
            double longitude,
            Instant forecastTime,
            Instant collectedAt,
            Double temperatureCelsius,
            Double relativeHumidity,
            Double precipitationMm,
            Double precipitationProbability,
            Double windSpeedKmh,
            Double windGustKmh,
            Double vapourPressureDeficitKpa,
            Double soilMoisture0To1cm,
            String source
    ) {

        private static CacheEntry parse(String value) {
            String[] parts = value.split("\\|", -1);

            if (parts.length != 13) {
                throw new IllegalArgumentException("Invalid weather snapshot cache entry");
            }

            return new CacheEntry(
                    Double.parseDouble(parts[0]),
                    Double.parseDouble(parts[1]),
                    Instant.parse(parts[2]),
                    Instant.parse(parts[3]),
                    nullableDouble(parts[4]),
                    nullableDouble(parts[5]),
                    nullableDouble(parts[6]),
                    nullableDouble(parts[7]),
                    nullableDouble(parts[8]),
                    nullableDouble(parts[9]),
                    nullableDouble(parts[10]),
                    nullableDouble(parts[11]),
                    parts[12]
            );
        }

        static CacheEntry from(WeatherSnapshotEntity snapshot) {
            return new CacheEntry(
                    snapshot.getLatitude(),
                    snapshot.getLongitude(),
                    snapshot.getForecastTime(),
                    snapshot.getCollectedAt(),
                    snapshot.getTemperatureCelsius(),
                    snapshot.getRelativeHumidity(),
                    snapshot.getPrecipitationMm(),
                    snapshot.getPrecipitationProbability(),
                    snapshot.getWindSpeedKmh(),
                    snapshot.getWindGustKmh(),
                    snapshot.getVapourPressureDeficitKpa(),
                    snapshot.getSoilMoisture0To1cm(),
                    snapshot.getSource()
            );
        }

        String serialize() {
            return String.join(
                    "|",
                    Double.toString(latitude),
                    Double.toString(longitude),
                    forecastTime.toString(),
                    collectedAt.toString(),
                    nullableString(temperatureCelsius),
                    nullableString(relativeHumidity),
                    nullableString(precipitationMm),
                    nullableString(precipitationProbability),
                    nullableString(windSpeedKmh),
                    nullableString(windGustKmh),
                    nullableString(vapourPressureDeficitKpa),
                    nullableString(soilMoisture0To1cm),
                    source
            );
        }

        WeatherSnapshotEntity toEntity() {
            return new WeatherSnapshotEntity(
                    latitude,
                    longitude,
                    forecastTime,
                    collectedAt,
                    temperatureCelsius,
                    relativeHumidity,
                    precipitationMm,
                    precipitationProbability,
                    windSpeedKmh,
                    windGustKmh,
                    vapourPressureDeficitKpa,
                    soilMoisture0To1cm,
                    source
            );
        }

        private static Double nullableDouble(String value) {
            if (value.isBlank()) {
                return null;
            }

            return Double.valueOf(value);
        }

        private static String nullableString(Double value) {
            if (value == null) {
                return "";
            }

            return value.toString();
        }
    }
}
