package com.climapulse.jceco.integration.openmeteo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoHourlyResponse(

        @JsonProperty("time")
        List<LocalDateTime> time,

        @JsonProperty("temperature_2m")
        List<Double> temperature2m,

        @JsonProperty("relative_humidity_2m")
        List<Double> relativeHumidity2m,

        @JsonProperty("precipitation")
        List<Double> precipitation,

        @JsonProperty("precipitation_probability")
        List<Double> precipitationProbability,

        @JsonProperty("wind_speed_10m")
        List<Double> windSpeed10m,

        @JsonProperty("wind_gusts_10m")
        List<Double> windGusts10m,

        @JsonProperty("vapour_pressure_deficit")
        List<Double> vapourPressureDeficit,

        @JsonProperty("soil_moisture_0_to_1cm")
        List<Double> soilMoisture0To1cm

) {
}
