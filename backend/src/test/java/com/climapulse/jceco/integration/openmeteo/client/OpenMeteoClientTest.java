package com.climapulse.jceco.integration.openmeteo.client;

import com.climapulse.jceco.integration.openmeteo.config.OpenMeteoProperties;
import com.climapulse.jceco.shared.exception.OpenMeteoClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoClientTest {

    private static final String FORECAST_RESPONSE = """
            {
              "latitude": -26.92,
              "longitude": -49.07,
              "timezone": "UTC",
              "elevation": 21.0,
              "hourly_units": {
                "time": "iso8601",
                "temperature_2m": "°C",
                "relative_humidity_2m": "%",
                "precipitation": "mm",
                "precipitation_probability": "%",
                "wind_speed_10m": "km/h",
                "wind_gusts_10m": "km/h",
                "vapour_pressure_deficit": "kPa",
                "soil_moisture_0_to_1cm": "m³/m³"
              },
              "hourly": {
                "time": ["2026-08-06T12:00", "2026-08-06T13:00"],
                "temperature_2m": [24.5, 25.1],
                "relative_humidity_2m": [62.0, 59.0],
                "precipitation": [0.0, 0.2],
                "precipitation_probability": [5.0, 20.0],
                "wind_speed_10m": [11.2, 13.4],
                "wind_gusts_10m": [18.0, 21.0],
                "vapour_pressure_deficit": [1.1, 1.3],
                "soil_moisture_0_to_1cm": [0.21, 0.20]
              },
              "unknown_field": "ignored"
            }
            """;

    private MockRestServiceServer server;
    private OpenMeteoClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        var properties = new OpenMeteoProperties(
                URI.create("https://api.open-meteo.test"),
                24,
                1
        );

        client = new OpenMeteoClient(properties, builder);
    }

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @Test
    void shouldSendForecastRequestWithExpectedQueryParameters() {
        server.expect(once(), request -> {
            URI uri = request.getURI();
            var queryParameters = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

            assertThat(uri.getScheme()).isEqualTo("https");
            assertThat(uri.getHost()).isEqualTo("api.open-meteo.test");
            assertThat(uri.getPath()).isEqualTo("/v1/forecast");
            assertThat(queryParameters.getFirst("latitude")).isEqualTo("-26.9189");
            assertThat(queryParameters.getFirst("longitude")).isEqualTo("-49.0661");
            assertThat(queryParameters.getFirst("hourly")).isEqualTo(
                    "temperature_2m,relative_humidity_2m,precipitation,precipitation_probability,"
                            + "wind_speed_10m,wind_gusts_10m,vapour_pressure_deficit,soil_moisture_0_to_1cm"
            );
            assertThat(queryParameters.getFirst("forecast_hours")).isEqualTo("24");
            assertThat(queryParameters.getFirst("past_hours")).isEqualTo("1");
            assertThat(queryParameters.getFirst("timezone")).isEqualTo("UTC");
            assertThat(queryParameters.getFirst("cell_selection")).isEqualTo("land");
        }).andRespond(withSuccess(FORECAST_RESPONSE, MediaType.APPLICATION_JSON));

        var response = client.fetchForecast(-26.9189, -49.0661);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldDeserializeForecastResponse() {
        server.expect(once(), anything()).andRespond(withSuccess(FORECAST_RESPONSE, MediaType.APPLICATION_JSON));

        var response = client.fetchForecast(-26.9189, -49.0661);

        assertThat(response.latitude()).isEqualTo(-26.92);
        assertThat(response.longitude()).isEqualTo(-49.07);
        assertThat(response.timezone()).isEqualTo("UTC");
        assertThat(response.elevation()).isEqualTo(21.0);
        assertThat(response.hourlyUnits().temperature2m()).isEqualTo("°C");
        assertThat(response.hourlyUnits().soilMoisture0To1cm()).isEqualTo("m³/m³");
        assertThat(response.hourly().time()).containsExactly(
                LocalDateTime.parse("2026-08-06T12:00"),
                LocalDateTime.parse("2026-08-06T13:00")
        );
        assertThat(response.hourly().temperature2m()).containsExactly(24.5, 25.1);
        assertThat(response.hourly().relativeHumidity2m()).containsExactly(62.0, 59.0);
        assertThat(response.hourly().precipitation()).containsExactly(0.0, 0.2);
        assertThat(response.hourly().precipitationProbability()).containsExactly(5.0, 20.0);
        assertThat(response.hourly().windSpeed10m()).containsExactly(11.2, 13.4);
        assertThat(response.hourly().windGusts10m()).containsExactly(18.0, 21.0);
        assertThat(response.hourly().vapourPressureDeficit()).containsExactly(1.1, 1.3);
        assertThat(response.hourly().soilMoisture0To1cm()).containsExactly(0.21, 0.20);
    }

    @Test
    void shouldThrowExceptionWhenResponseBodyIsEmpty() {
        server.expect(once(), anything()).andRespond(withStatus(HttpStatus.OK));

        assertThatThrownBy(() -> client.fetchForecast(-26.9189, -49.0661))
                .isInstanceOf(OpenMeteoClientException.class)
                .hasMessage("Open-Meteo returned an empty response");
    }

    @Test
    void shouldWrapExceptionWhenOpenMeteoReturnsServerError() {
        server.expect(once(), anything()).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchForecast(-26.9189, -49.0661))
                .isInstanceOf(OpenMeteoClientException.class)
                .hasMessage("Could not communicate with Open-Meteo")
                .hasCauseInstanceOf(RestClientException.class);
    }
}
