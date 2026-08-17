package com.climapulse.jceco.integration.inpe.persistence;

import com.climapulse.jceco.integration.inpe.model.InpeHotspot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hotspot", schema = "climapulse")
public class HotspotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_filename", nullable = false, length = 80)
    private String sourceFilename;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false, length = 60)
    private String satellite;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    protected HotspotEntity() {
    }

    public HotspotEntity(String sourceFilename, InpeHotspot hotspot) {
        this.sourceFilename = sourceFilename;
        this.latitude = hotspot.latitude();
        this.longitude = hotspot.longitude();
        this.satellite = hotspot.satellite();
        this.observedAt = hotspot.observedAt();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}