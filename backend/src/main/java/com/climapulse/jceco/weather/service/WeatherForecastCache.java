package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.weather.config.WeatherProperties;
import com.climapulse.jceco.weather.persistence.WeatherForecastEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
class WeatherForecastCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherForecastCache.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    WeatherForecastCache(
            StringRedisTemplate redisTemplate,
            WeatherProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.ttl = properties.forecastTtl();
    }

    Optional<WeatherForecastEntity> get(double latitude, double longitude, Instant forecastTime) {
        String key = key(latitude, longitude, forecastTime);

        try {
            String cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                return Optional.empty();
            }

            WeatherForecastCacheEntry entry = WeatherForecastCacheEntry.parse(cachedValue);

            return Optional.of(WeatherForecastCacheEntryMapper.toEntity(entry));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not read weather forecast from Redis: {}", key, exception);
            return Optional.empty();
        }
    }

    void put(WeatherForecastEntity forecast) {
        String key = key(forecast.getLatitude(), forecast.getLongitude(), forecast.getForecastTime());

        try {
            WeatherForecastCacheEntry entry = WeatherForecastCacheEntryMapper.fromEntity(forecast);
            String value = entry.serialize();
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not save weather forecast in Redis: {}", key, exception);
        }
    }

    private String key(double latitude, double longitude, Instant forecastTime) {
        return "weather:forecast:" + latitude + ":" + longitude + ":" + forecastTime;
    }
}
