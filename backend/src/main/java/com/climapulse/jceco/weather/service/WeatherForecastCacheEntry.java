package com.climapulse.jceco.weather.service;

import java.time.Instant;

record WeatherForecastCacheEntry(
        double latitude,
        double longitude,
        Instant forecastTime,
        Instant collectedAt,
        Double temperatureCelsius,
        Double relativeHumidity,
        Double precipitationMm,
        Double precipitationProbability,
        Double windSpeedKmh,
        Double windGustKmh,
        Double vapourPressureDeficitKpa,
        Double soilMoisture0To1cm,
        String source
) {

    static WeatherForecastCacheEntry parse(String value) {
        String[] parts = value.split("\\|", -1);

        if (parts.length != 13) {
            throw new IllegalArgumentException("Invalid weather forecast cache entry");
        }

        return new WeatherForecastCacheEntry(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Instant.parse(parts[2]),
                Instant.parse(parts[3]),
                nullableDouble(parts[4]),
                nullableDouble(parts[5]),
                nullableDouble(parts[6]),
                nullableDouble(parts[7]),
                nullableDouble(parts[8]),
                nullableDouble(parts[9]),
                nullableDouble(parts[10]),
                nullableDouble(parts[11]),
                parts[12]
        );
    }

    String serialize() {
        return String.join(
                "|",
                Double.toString(latitude),
                Double.toString(longitude),
                forecastTime.toString(),
                collectedAt.toString(),
                nullableString(temperatureCelsius),
                nullableString(relativeHumidity),
                nullableString(precipitationMm),
                nullableString(precipitationProbability),
                nullableString(windSpeedKmh),
                nullableString(windGustKmh),
                nullableString(vapourPressureDeficitKpa),
                nullableString(soilMoisture0To1cm),
                source
        );
    }

    private static Double nullableDouble(String value) {
        if (value.isBlank()) {
            return null;
        }

        return Double.valueOf(value);
    }

    private static String nullableString(Double value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
