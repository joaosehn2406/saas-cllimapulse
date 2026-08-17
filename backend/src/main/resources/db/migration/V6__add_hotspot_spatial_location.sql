ALTER TABLE climapulse.hotspot
    ADD COLUMN location GEOGRAPHY(Point, 4326);

UPDATE climapulse.hotspot
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography;

ALTER TABLE climapulse.hotspot
    ALTER COLUMN location SET NOT NULL;

CREATE OR REPLACE FUNCTION climapulse.set_hotspot_location()
RETURNS trigger AS $$
BEGIN
    NEW.location := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hotspot_set_location
BEFORE INSERT OR UPDATE OF latitude, longitude
ON climapulse.hotspot
FOR EACH ROW
EXECUTE FUNCTION climapulse.set_hotspot_location();

CREATE INDEX idx_hotspot_location
    ON climapulse.hotspot
    USING GIST (location);