package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.integration.openmeteo.client.OpenMeteoClient;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoForecastResponse;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoHourlyResponse;
import com.climapulse.jceco.shared.exception.InvalidCoordinateException;
import com.climapulse.jceco.weather.config.WeatherProperties;
import com.climapulse.jceco.weather.persistence.WeatherForecastEntity;
import com.climapulse.jceco.weather.persistence.WeatherForecastRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherForecastServiceTest {

    @Mock
    private OpenMeteoClient openMeteoClient;

    @Mock
    private WeatherForecastRepository weatherForecastRepository;

    @Mock
    private WeatherForecastCache weatherForecastCache;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-08T14:37:00Z"), ZoneOffset.UTC);
    private final WeatherProperties properties = new WeatherProperties(Duration.ofHours(1));

    @Test
    void shouldReturnForecastFromCacheWhenAvailable() {
        var forecast = forecastEntity(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z"));
        var service = service();

        when(weatherForecastCache.get(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z")))
                .thenReturn(Optional.of(forecast));

        var response = service.getCurrentForecast(-26.9189, -49.0661);

        assertThat(response).isSameAs(forecast);
        verifyNoInteractions(weatherForecastRepository, openMeteoClient);
    }

    @Test
    void shouldReturnPersistedForecastAndRefreshCacheWhenCacheMisses() {
        var forecast = forecastEntity(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z"));
        var service = service();

        when(weatherForecastCache.get(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z")))
                .thenReturn(Optional.empty());
        when(weatherForecastRepository
                .findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
                        -26.9189,
                        -49.0661,
                        Instant.parse("2026-09-08T14:00:00Z"),
                        Instant.parse("2026-09-08T13:37:00Z"),
                        "OPEN_METEO"
                ))
                .thenReturn(Optional.of(forecast));

        var response = service.getCurrentForecast(-26.9189, -49.0661);

        assertThat(response).isSameAs(forecast);
        verify(weatherForecastCache).put(forecast);
        verifyNoInteractions(openMeteoClient);
    }

    @Test
    void shouldFetchAndSaveForecastWhenCacheAndDatabaseMiss() {
        var forecast = forecast();
        var savedForecast = forecastEntity(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z"));
        var service = service();

        when(weatherForecastCache.get(-26.9189, -49.0661, Instant.parse("2026-09-08T14:00:00Z")))
                .thenReturn(Optional.empty());
        when(weatherForecastRepository
                .findFirstByLatitudeAndLongitudeAndForecastTimeAndCollectedAtGreaterThanEqualAndSourceOrderByCollectedAtDesc(
                        -26.9189,
                        -49.0661,
                        Instant.parse("2026-09-08T14:00:00Z"),
                        Instant.parse("2026-09-08T13:37:00Z"),
                        "OPEN_METEO"
                ))
                .thenReturn(Optional.empty());
        when(openMeteoClient.fetchForecast(-26.9189, -49.0661)).thenReturn(forecast);
        when(weatherForecastRepository.save(org.mockito.ArgumentMatchers.any(WeatherForecastEntity.class)))
                .thenReturn(savedForecast);

        var response = service.getCurrentForecast(-26.9189, -49.0661);

        assertThat(response).isSameAs(savedForecast);
        verify(openMeteoClient).fetchForecast(-26.9189, -49.0661);
        verify(weatherForecastCache).put(savedForecast);
    }

    @Test
    void shouldRejectInvalidCoordinates() {
        var service = service();

        assertThatThrownBy(() -> service.getCurrentForecast(-91, -49.0661))
                .isInstanceOf(InvalidCoordinateException.class);

        verifyNoInteractions(weatherForecastCache, weatherForecastRepository, openMeteoClient);
    }

    private WeatherForecastService service() {
        return new WeatherForecastService(
                openMeteoClient,
                weatherForecastRepository,
                weatherForecastCache,
                properties,
                clock
        );
    }

    private OpenMeteoForecastResponse forecast() {
        return new OpenMeteoForecastResponse(
                -26.92,
                -49.07,
                "UTC",
                21.0,
                null,
                new OpenMeteoHourlyResponse(
                        List.of(
                                LocalDateTime.parse("2026-09-08T13:00"),
                                LocalDateTime.parse("2026-09-08T14:00"),
                                LocalDateTime.parse("2026-09-08T15:00")
                        ),
                        List.of(24.0, 25.0, 26.0),
                        List.of(45.0, 40.0, 38.0),
                        List.of(0.0, 0.0, 0.1),
                        List.of(10.0, 5.0, 20.0),
                        List.of(11.0, 12.0, 13.0),
                        List.of(18.0, 19.0, 20.0),
                        List.of(1.1, 1.2, 1.3),
                        List.of(0.21, 0.20, 0.19)
                )
        );
    }

    private WeatherForecastEntity forecastEntity(double latitude, double longitude, Instant forecastTime) {
        return new WeatherForecastEntity(
                latitude,
                longitude,
                forecastTime,
                Instant.parse("2026-09-08T14:37:00Z"),
                25.0,
                40.0,
                0.0,
                5.0,
                12.0,
                19.0,
                1.2,
                0.20,
                "OPEN_METEO"
        );
    }
}
