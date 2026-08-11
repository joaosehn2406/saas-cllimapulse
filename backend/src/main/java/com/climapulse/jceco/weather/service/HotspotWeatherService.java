package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.integration.openmeteo.client.OpenMeteoClient;
import com.climapulse.jceco.integration.openmeteo.model.OpenMeteoForecastResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HotspotWeatherService {

    private final HotspotRepository hotspotRepository;
    private final OpenMeteoClient openMeteoClient;

    public HotspotWeatherService(HotspotRepository hotspotRepository, OpenMeteoClient openMeteoClient) {
        this.hotspotRepository = hotspotRepository;
        this.openMeteoClient = openMeteoClient;
    }

    public OpenMeteoForecastResponse getForecast(UUID hotspotId) {
        
    }
}
