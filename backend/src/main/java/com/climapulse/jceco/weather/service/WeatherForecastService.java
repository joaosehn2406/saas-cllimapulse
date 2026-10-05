package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.integration.openmeteo.client.OpenMeteoClient;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoForecastResponse;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoHourlyResponse;
import com.climapulse.jceco.shared.exception.InvalidCoordinateException;
import com.climapulse.jceco.shared.exception.OpenMeteoClientException;
import com.climapulse.jceco.weather.config.WeatherProperties;
import com.climapulse.jceco.weather.persistence.WeatherForecastEntity;
import com.climapulse.jceco.weather.persistence.WeatherForecastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WeatherForecastService {

    private static final String SOURCE = "OPEN_METEO";

    private final OpenMeteoClient openMeteoClient;
    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherForecastCache weatherForecastCache;
    private final WeatherProperties properties;
    private final Clock clock;

    @Autowired
    public WeatherForecastService(
            OpenMeteoClient openMeteoClient,
            WeatherForecastRepository weatherForecastRepository,
            WeatherForecastCache weatherForecastCache,
            WeatherProperties properties
    ) {
        this(openMeteoClient, weatherForecastRepository, weatherForecastCache, properties, Clock.systemUTC());
    }

    WeatherForecastService(
            OpenMeteoClient openMeteoClient,
            WeatherForecastRepository weatherForecastRepository,
            WeatherForecastCache weatherForecastCache,
            WeatherProperties properties,
            Clock clock
    ) {
        this.openMeteoClient = openMeteoClient;
        this.weatherForecastRepository = weatherForecastRepository;
        this.weatherForecastCache = weatherForecastCache;
        this.properties = properties;
        this.clock = clock;
    }

    public WeatherForecastEntity getCurrentForecast(double latitude, double longitude) {
        validateCoordinate(latitude, longitude);

        Instant now = clock.instant();
        Instant forecastTime = now.truncatedTo(ChronoUnit.HOURS);
        Instant collectedAfter = now.minus(properties.forecastTtl());

        return weatherForecastCache.get(latitude, longitude, forecastTime)
                .or(() -> findPersistedForecast(latitude, longitude, forecastTime, collectedAfter))
                .orElseGet(() -> fetchAndSaveForecast(latitude, longitude, forecastTime, now));
    }

    private java.util.Optional<WeatherForecastEntity> findPersistedForecast(
            double latitude,
            double longitude,
            Instant forecastTime,
            Instant collectedAfter
    ) {
        var forecast = weatherForecastRepository
                .findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
                        latitude,
                        longitude,
                        forecastTime,
                        collectedAfter,
                        SOURCE
                );

        forecast.ifPresent(weatherForecastCache::put);

        return forecast;
    }

    private WeatherForecastEntity fetchAndSaveForecast(
            double latitude,
            double longitude,
            Instant preferredForecastTime,
            Instant collectedAt
    ) {
        OpenMeteoForecastResponse response = openMeteoClient.fetchForecast(latitude, longitude);
        WeatherForecastEntity forecast = toForecast(latitude, longitude, preferredForecastTime, collectedAt, response);
        WeatherForecastEntity savedForecast = weatherForecastRepository.save(forecast);

        weatherForecastCache.put(savedForecast);

        return savedForecast;
    }

    private WeatherForecastEntity toForecast(
            double latitude,
            double longitude,
            Instant preferredForecastTime,
            Instant collectedAt,
            OpenMeteoForecastResponse response
    ) {
        OpenMeteoHourlyResponse hourly = response.hourly();

        if (hourly == null || hourly.time() == null || hourly.time().isEmpty()) {
            throw new OpenMeteoClientException("Open-Meteo returned forecast without hourly data");
        }

        int index = resolveForecastIndex(hourly.time(), preferredForecastTime);
        Instant forecastTime = hourly.time().get(index).toInstant(ZoneOffset.UTC);

        return new WeatherForecastEntity(
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

    private void validateCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException(latitude, longitude);
        }
    }
}
