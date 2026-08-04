package com.climapulse.jceco.integration.openmeteo.client;

import com.climapulse.jceco.integration.openmeteo.config.OpenMeteoProperties;
import com.climapulse.jceco.integration.openmeteo.model.response.OpenMeteoForecastResponse;
import com.climapulse.jceco.shared.exception.OpenMeteoClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

    public OpenMeteoForecastResponse fetchForecast(double latitude, double longitude) {
        try {
            OpenMeteoForecastResponse response = restClient.get()
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
                            .build())
                    .retrieve()
                    .body(OpenMeteoForecastResponse.class);

            if (response == null) {
                throw new OpenMeteoClientException(
                        "Open-Meteo returned an empty response"
                );
            }

            return response;
        } catch (RestClientException exception) {
            throw new OpenMeteoClientException(
                    "Could not communicate with Open-Meteo",
                    exception
            );
        }
    }
}