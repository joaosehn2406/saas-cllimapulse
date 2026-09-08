package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.integration.openmeteo.client.OpenMeteoClient;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoForecastResponse;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoHourlyResponse;
import com.climapulse.jceco.shared.exception.InvalidCoordinateException;
import com.climapulse.jceco.shared.exception.OpenMeteoClientException;
import com.climapulse.jceco.weather.config.WeatherProperties;
import com.climapulse.jceco.weather.persistence.WeatherSnapshotEntity;
import com.climapulse.jceco.weather.persistence.WeatherSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WeatherSnapshotService {

    private static final String SOURCE = "OPEN_METEO";

    private final OpenMeteoClient openMeteoClient;
    private final WeatherSnapshotRepository weatherSnapshotRepository;
    private final WeatherSnapshotCache weatherSnapshotCache;
    private final WeatherProperties properties;
    private final Clock clock;

    @Autowired
    public WeatherSnapshotService(
            OpenMeteoClient openMeteoClient,
            WeatherSnapshotRepository weatherSnapshotRepository,
            WeatherSnapshotCache weatherSnapshotCache,
            WeatherProperties properties
    ) {
        this(openMeteoClient, weatherSnapshotRepository, weatherSnapshotCache, properties, Clock.systemUTC());
    }

    WeatherSnapshotService(
            OpenMeteoClient openMeteoClient,
            WeatherSnapshotRepository weatherSnapshotRepository,
            WeatherSnapshotCache weatherSnapshotCache,
            WeatherProperties properties,
            Clock clock
    ) {
        this.openMeteoClient = openMeteoClient;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
        this.weatherSnapshotCache = weatherSnapshotCache;
        this.properties = properties;
        this.clock = clock;
    }

    public WeatherSnapshotEntity getCurrentSnapshot(double latitude, double longitude) {
        validateCoordinate(latitude, longitude);

        double roundedLatitude = round(latitude);
        double roundedLongitude = round(longitude);
        Instant now = clock.instant();
        Instant forecastTime = now.truncatedTo(ChronoUnit.HOURS);
        Instant collectedAfter = now.minus(properties.snapshotTtl());

        return weatherSnapshotCache.get(roundedLatitude, roundedLongitude, forecastTime)
                .or(() -> findPersistedSnapshot(roundedLatitude, roundedLongitude, forecastTime, collectedAfter))
                .orElseGet(() -> fetchAndSaveSnapshot(roundedLatitude, roundedLongitude, forecastTime, now));
    }

    private java.util.Optional<WeatherSnapshotEntity> findPersistedSnapshot(
            double latitude,
            double longitude,
            Instant forecastTime,
            Instant collectedAfter
    ) {
        var snapshot = weatherSnapshotRepository
                .findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
                        latitude,
                        longitude,
                        forecastTime,
                        collectedAfter,
                        SOURCE
                );

        snapshot.ifPresent(weatherSnapshotCache::put);

        return snapshot;
    }

    private WeatherSnapshotEntity fetchAndSaveSnapshot(
            double latitude,
            double longitude,
            Instant preferredForecastTime,
            Instant collectedAt
    ) {
        OpenMeteoForecastResponse forecast = openMeteoClient.fetchForecast(latitude, longitude);
        WeatherSnapshotEntity snapshot = toSnapshot(latitude, longitude, preferredForecastTime, collectedAt, forecast);
        WeatherSnapshotEntity savedSnapshot = weatherSnapshotRepository.save(snapshot);

        weatherSnapshotCache.put(savedSnapshot);

        return savedSnapshot;
    }

    private WeatherSnapshotEntity toSnapshot(
            double latitude,
            double longitude,
            Instant preferredForecastTime,
            Instant collectedAt,
            OpenMeteoForecastResponse forecast
    ) {
        OpenMeteoHourlyResponse hourly = forecast.hourly();

        if (hourly == null || hourly.time() == null || hourly.time().isEmpty()) {
            throw new OpenMeteoClientException("Open-Meteo returned forecast without hourly data");
        }

        int index = resolveForecastIndex(hourly.time(), preferredForecastTime);
        Instant forecastTime = hourly.time().get(index).toInstant(ZoneOffset.UTC);

        return new WeatherSnapshotEntity(
                latitude,
                longitude,
                forecastTime,
                collectedAt,
                valueAt(hourly.temperature2m(), index),
                valueAt(hourly.relativeHumidity2m(), index),
                valueAt(hourly.precipitation(), index),
                valueAt(hourly.precipitationProbability(), index),
                valueAt(hourly.windSpeed10m(), index),
                valueAt(hourly.windGusts10m(), index),
                valueAt(hourly.vapourPressureDeficit(), index),
                valueAt(hourly.soilMoisture0To1cm(), index),
                SOURCE
        );
    }

    private int resolveForecastIndex(List<LocalDateTime> forecastTimes, Instant preferredForecastTime) {
        LocalDateTime preferredTime = LocalDateTime.ofInstant(preferredForecastTime, ZoneOffset.UTC);

        for (int index = 0; index < forecastTimes.size(); index++) {
            if (!forecastTimes.get(index).isBefore(preferredTime)) {
                return index;
            }
        }

        return forecastTimes.size() - 1;
    }

    private Double valueAt(List<Double> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }

        return values.get(index);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(properties.coordinateScale(), RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void validateCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException(latitude, longitude);
        }
    }
}
