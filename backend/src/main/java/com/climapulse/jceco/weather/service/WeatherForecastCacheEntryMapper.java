package com.climapulse.jceco.weather.service;

import com.climapulse.jceco.weather.persistence.WeatherForecastEntity;

final class WeatherForecastCacheEntryMapper {

    private WeatherForecastCacheEntryMapper() {
    }

    static WeatherForecastCacheEntry fromEntity(WeatherForecastEntity forecast) {
        return new WeatherForecastCacheEntry(
                forecast.getLatitude(),
                forecast.getLongitude(),
                forecast.getForecastTime(),
                forecast.getCollectedAt(),
                forecast.getTemperatureCelsius(),
                forecast.getRelativeHumidity(),
                forecast.getPrecipitationMm(),
                forecast.getPrecipitationProbability(),
                forecast.getWindSpeedKmh(),
                forecast.getWindGustKmh(),
                forecast.getVapourPressureDeficitKpa(),
                forecast.getSoilMoisture0To1cm(),
                forecast.getSource()
        );
    }

    static WeatherForecastEntity toEntity(WeatherForecastCacheEntry entry) {
        return new WeatherForecastEntity(
                entry.latitude(),
                entry.longitude(),
                entry.forecastTime(),
                entry.collectedAt(),
                entry.temperatureCelsius(),
                entry.relativeHumidity(),
                entry.precipitationMm(),
                entry.precipitationProbability(),
                entry.windSpeedKmh(),
                entry.windGustKmh(),
                entry.vapourPressureDeficitKpa(),
                entry.soilMoisture0To1cm(),
                entry.source()
        );
    }
}
