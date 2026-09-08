package com.climapulse.jceco.weather.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "weather_snapshot", schema = "climapulse")
public class WeatherSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "forecast_time", nullable = false)
    private Instant forecastTime;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "relative_humidity")
    private Double relativeHumidity;

    @Column(name = "precipitation_mm")
    private Double precipitationMm;

    @Column(name = "precipitation_probability")
    private Double precipitationProbability;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "wind_gust_kmh")
    private Double windGustKmh;

    @Column(name = "vapour_pressure_deficit_kpa")
    private Double vapourPressureDeficitKpa;

    @Column(name = "soil_moisture_0_to_1cm")
    private Double soilMoisture0To1cm;

    @Column(nullable = false, length = 40)
    private String source;

    protected WeatherSnapshotEntity() {
    }

    public WeatherSnapshotEntity(
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
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecastTime = forecastTime;
        this.collectedAt = collectedAt;
        this.temperatureCelsius = temperatureCelsius;
        this.relativeHumidity = relativeHumidity;
        this.precipitationMm = precipitationMm;
        this.precipitationProbability = precipitationProbability;
        this.windSpeedKmh = windSpeedKmh;
        this.windGustKmh = windGustKmh;
        this.vapourPressureDeficitKpa = vapourPressureDeficitKpa;
        this.soilMoisture0To1cm = soilMoisture0To1cm;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Instant getForecastTime() {
        return forecastTime;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public Double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public Double getRelativeHumidity() {
        return relativeHumidity;
    }

    public Double getPrecipitationMm() {
        return precipitationMm;
    }

    public Double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public Double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public Double getWindGustKmh() {
        return windGustKmh;
    }

    public Double getVapourPressureDeficitKpa() {
        return vapourPressureDeficitKpa;
    }

    public Double getSoilMoisture0To1cm() {
        return soilMoisture0To1cm;
    }

    public String getSource() {
        return source;
    }
}
