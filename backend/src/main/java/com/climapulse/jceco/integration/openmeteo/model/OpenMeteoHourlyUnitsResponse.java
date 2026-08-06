package com.climapulse.jceco.integration.openmeteo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoHourlyUnitsResponse(

        @JsonProperty("time")
        String time,

        @JsonProperty("temperature_2m")
        String temperature2m,

        @JsonProperty("relative_humidity_2m")
        String relativeHumidity2m,

        @JsonProperty("precipitation")
        String precipitation,

        @JsonProperty("precipitation_probability")
        String precipitationProbability,

        @JsonProperty("wind_speed_10m")
        String windSpeed10m,

        @JsonProperty("wind_gusts_10m")
        String windGusts10m,

        @JsonProperty("vapour_pressure_deficit")
        String vapourPressureDeficit,

        @JsonProperty("soil_moisture_0_to_1cm")
        String soilMoisture0To1cm

) {
}
