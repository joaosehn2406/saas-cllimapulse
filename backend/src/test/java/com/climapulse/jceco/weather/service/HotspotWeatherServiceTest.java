package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.integration.inpe.model.InpeHotspot;
import com.climapulse.jceco.integration.inpe.persistence.HotspotEntity;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.integration.openmeteo.client.OpenMeteoClient;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoForecastResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotspotWeatherServiceTest {

    @Mock
    private HotspotRepository hotspotRepository;

    @Mock
    private OpenMeteoClient openMeteoClient;

    @Test
    void shouldFetchForecastUsingHotspotCoordinates() {
        var hotspotId = UUID.randomUUID();
        var hotspot = new HotspotEntity(
                "focos_10min_20260707_0250.csv",
                new InpeHotspot(-26.9189, -49.0661, "GOES-19", Instant.parse("2026-07-07T02:50:00Z"))
        );
        var forecast = new OpenMeteoForecastResponse(-26.92, -49.07, "UTC", 21.0, null, null);
        var service = new HotspotWeatherService(hotspotRepository, openMeteoClient);

        when(hotspotRepository.findById(hotspotId)).thenReturn(Optional.of(hotspot));
        when(openMeteoClient.fetchForecast(-26.9189, -49.0661)).thenReturn(forecast);

        var response = service.getForecast(hotspotId);

        assertThat(response).isSameAs(forecast);
        verify(openMeteoClient).fetchForecast(-26.9189, -49.0661);
    }

    @Test
    void shouldThrowExceptionWhenHotspotDoesNotExist() {
        var hotspotId = UUID.randomUUID();
        var service = new HotspotWeatherService(hotspotRepository, openMeteoClient);

        when(hotspotRepository.findById(hotspotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForecast(hotspotId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Hotspot not found: " + hotspotId);

        verifyNoInteractions(openMeteoClient);
    }
}