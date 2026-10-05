ALTER TABLE climapulse.weather_snapshot
    RENAME TO weather_forecast;

ALTER TABLE climapulse.weather_forecast
    RENAME CONSTRAINT uq_weather_snapshot_point_forecast_source
    TO uq_weather_forecast_point_time_source;

ALTER INDEX climapulse.idx_weather_snapshot_point_forecast
    RENAME TO idx_weather_forecast_point_time;

ALTER INDEX climapulse.idx_weather_snapshot_collected_at
    RENAME TO idx_weather_forecast_collected_at;
