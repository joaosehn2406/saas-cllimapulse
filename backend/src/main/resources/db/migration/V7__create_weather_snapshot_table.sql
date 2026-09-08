CREATE TABLE climapulse.weather_snapshot
(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    forecast_time timestamptz NOT NULL,
    collected_at timestamptz NOT NULL DEFAULT now(),
    temperature_celsius double precision,
    relative_humidity double precision,
    precipitation_mm double precision,
    precipitation_probability double precision,
    wind_speed_kmh double precision,
    wind_gust_kmh double precision,
    vapour_pressure_deficit_kpa double precision,
    soil_moisture_0_to_1cm double precision,
    source varchar(40) NOT NULL,
    CONSTRAINT uq_weather_snapshot_point_forecast_source
        UNIQUE (latitude, longitude, forecast_time, source)
);

CREATE INDEX idx_weather_snapshot_point_forecast
    ON climapulse.weather_snapshot (latitude, longitude, forecast_time DESC);

CREATE INDEX idx_weather_snapshot_collected_at
    ON climapulse.weather_snapshot (collected_at DESC);
