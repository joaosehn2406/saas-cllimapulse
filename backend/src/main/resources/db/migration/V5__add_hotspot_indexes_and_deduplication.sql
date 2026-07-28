ALTER TABLE climapulse.hotspot
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE climapulse.hotspot
    ADD CONSTRAINT uq_hotspot_observation
        UNIQUE (latitude, longitude, satellite, observed_at);

CREATE INDEX idx_hotspot_observed_at
    ON climapulse.hotspot (observed_at);

CREATE INDEX idx_hotspot_source_filename
    ON climapulse.hotspot (source_filename);
