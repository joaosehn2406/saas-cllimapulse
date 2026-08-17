package com.climapulse.jceco.integration.inpe.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface HotspotRepository extends JpaRepository<HotspotEntity, UUID> {

    Page<HotspotEntity> findAllByOrderByObservedAtDesc(Pageable pageable);

    Page<HotspotEntity> findByObservedAtBetweenOrderByObservedAtDesc(
            Instant from,
            Instant to,
            Pageable pageable
    );

    Page<HotspotEntity> findBySatelliteIgnoreCaseOrderByObservedAtDesc(
            String satellite,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        hotspot.id,
                        hotspot.source_filename,
                        hotspot.latitude,
                        hotspot.longitude,
                        hotspot.satellite,
                        hotspot.observed_at,
                        hotspot.created_at
                    FROM climapulse.hotspot hotspot
                    WHERE ST_DWithin(
                        hotspot.location,
                        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                        :radiusMeters
                    )
                    ORDER BY hotspot.observed_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM climapulse.hotspot h
                    WHERE ST_DWithin(
                        h.location,
                        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                        :radiusMeters
                    )
                    """,
            nativeQuery = true
    )
    Page<HotspotEntity> findWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters,
            Pageable pageable
    );
}