CREATE TABLE climapulse.risk_assessment
(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    radius_meters double precision NOT NULL,
    recent_hotspot_count bigint NOT NULL,
    score integer NOT NULL,
    level varchar(20) NOT NULL,
    hotspot_factor double precision NOT NULL,
    humidity_factor double precision,
    dry_days_factor double precision,
    no_rain_forecast_factor double precision,
    calculated_at timestamptz NOT NULL,
    valid_until timestamptz NOT NULL,
    methodology_version varchar(20) NOT NULL,
    CONSTRAINT ck_risk_assessment_score
        CHECK (score >= 0 AND score <= 100),
    CONSTRAINT ck_risk_assessment_radius_meters
        CHECK (radius_meters > 0)
);

CREATE INDEX idx_risk_assessment_point_calculated_at
    ON climapulse.risk_assessment (latitude, longitude, calculated_at DESC);

CREATE INDEX idx_risk_assessment_level_calculated_at
    ON climapulse.risk_assessment (level, calculated_at DESC);
