package com.climapulse.jceco.integration.openmeteo.client;

import com.climapulse.jceco.integration.openmeteo.config.OpenMeteoProperties;
import com.climapulse.jceco.integration.openmeteo.model.response.OpenMeteoForecastResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private static final String HOURLY_VARIABLES = String.join(
            ",",
            "temperature_2m",
            "relative_humidity_2m",
            "precipitation",
            "precipitation_probability",
            "wind_speed_10m",
            "wind_gusts_10m",
            "vapour_pressure_deficit",
            "soil_moisture_0_to_1cm"
    );

    private final RestClient restClient;
    private final OpenMeteoProperties properties;

    public OpenMeteoClient(
            OpenMeteoProperties properties,
            RestClient.Builder builder
    ) {
        this.properties = properties;

        this.restClient = builder
                .baseUrl(properties.baseUrl().toString())
                .build();
    }

    public OpenMeteoForecastResponse fetchForecast(
            double latitude,
            double longitude
    ) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", HOURLY_VARIABLES)
                        .queryParam(
                                "forecast_hours",
                                properties.forecastHours()
                        )
                        .queryParam(
                                "past_hours",
                                properties.pastHours()
                        )
                        .queryParam("timezone", "UTC")
                        .queryParam("cell_selection", "land")
                        .queryParam("temperature_unit", "celsius")
                        .queryParam("wind_speed_unit", "kmh")
                        .queryParam("precipitation_unit", "mm")
                        .build())
                .retrieve()
                .body(OpenMeteoForecastResponse.class);
    }
}