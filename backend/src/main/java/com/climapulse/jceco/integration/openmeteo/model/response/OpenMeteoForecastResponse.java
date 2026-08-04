package com.climapulse.jceco.integration.openmeteo.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoForecastResponse(

        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("timezone")
        String timezone,

        @JsonProperty("elevation")
        double elevation,

        @JsonProperty("hourly_units")
        OpenMeteoHourlyUnitsResponse hourlyUnits,

        @JsonProperty("hourly")
        OpenMeteoHourlyResponse hourly

) {
}
