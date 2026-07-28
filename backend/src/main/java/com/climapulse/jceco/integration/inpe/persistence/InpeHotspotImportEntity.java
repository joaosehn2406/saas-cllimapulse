package com.climapulse.jceco.integration.inpe.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "inpe_file", schema = "climapulse")
public class InpeHotspotImportEntity {

    @Id
    @Column(length = 80, nullable = false)
    private String filename;

    @Column(
            name = "imported_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant importedAt;

    protected InpeHotspotImportEntity() {
    }

    public InpeHotspotImportEntity(String filename) {
        this.filename = filename;
    }
}