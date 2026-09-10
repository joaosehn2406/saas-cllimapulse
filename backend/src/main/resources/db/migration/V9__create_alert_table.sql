CREATE TABLE climapulse.alert
(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    risk_assessment_id uuid NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    radius_meters double precision NOT NULL,
    severity varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    title varchar(120) NOT NULL,
    message varchar(500) NOT NULL,
    created_at timestamptz NOT NULL,
    resolved_at timestamptz,
    CONSTRAINT fk_alert_risk_assessment
        FOREIGN KEY (risk_assessment_id)
        REFERENCES climapulse.risk_assessment(id),
    CONSTRAINT ck_alert_radius_meters
        CHECK (radius_meters > 0)
);

CREATE UNIQUE INDEX uq_alert_open_area
    ON climapulse.alert (latitude, longitude, radius_meters)
    WHERE status = 'OPEN';

CREATE INDEX idx_alert_status_created_at
    ON climapulse.alert (status, created_at DESC);

CREATE INDEX idx_alert_severity_created_at
    ON climapulse.alert (severity, created_at DESC);
