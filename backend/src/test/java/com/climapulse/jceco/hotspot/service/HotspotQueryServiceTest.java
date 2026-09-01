package com.climapulse.jceco.hotspot.service;

import com.climapulse.jceco.integration.inpe.model.InpeHotspot;
import com.climapulse.jceco.integration.inpe.persistence.HotspotEntity;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotspotQueryServiceTest {

    @Mock
    private HotspotRepository hotspotRepository;

    @Test
    void shouldFindFirstPageOfRecentHotspotsByDefault() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(0, 20);
        var hotspot = hotspot(
                UUID.randomUUID(),
                "focos_10min_20260707_0250.csv",
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        when(hotspotRepository.findAllByOrderByObservedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(hotspot), pageable, 1));

        var page = service.findRecent();

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getId()).isEqualTo(hotspot.getId());
        assertThat(page.getContent().getFirst().getLatitude()).isEqualTo(-26.9189);
        assertThat(page.getContent().getFirst().getLongitude()).isEqualTo(-49.0661);
        assertThat(page.getContent().getFirst().getSatellite()).isEqualTo("GOES-19");
        assertThat(page.getContent().getFirst().getObservedAt()).isEqualTo(Instant.parse("2026-07-07T02:50:00Z"));
        assertThat(page.getContent().getFirst().getSourceFilename()).isEqualTo("focos_10min_20260707_0250.csv");
    }

    @Test
    void shouldFindRequestedPageOfRecentHotspots() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(2, 20);

        when(hotspotRepository.findAllByOrderByObservedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 40));

        var page = service.findRecent(2);

        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void shouldFindHotspotsByPeriod() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(1, 20);
        var from = Instant.parse("2026-07-07T00:00:00Z");
        var to = Instant.parse("2026-07-08T00:00:00Z");
        var hotspot = hotspot(
                UUID.randomUUID(),
                "focos_10min_20260707_0250.csv",
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        when(hotspotRepository.findByObservedAtBetweenOrderByObservedAtDesc(from, to, pageable))
                .thenReturn(new PageImpl<>(List.of(hotspot), pageable, 1));

        var page = service.findByPeriod(from, to, 1);

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(21);
        assertThat(page.getContent().getFirst().getObservedAt()).isBetween(from, to);
    }

    @Test
    void shouldFindHotspotsBySatellite() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(1, 20);
        var hotspot = hotspot(
                UUID.randomUUID(),
                "focos_10min_20260707_0250.csv",
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        when(hotspotRepository.findBySatelliteIgnoreCaseOrderByObservedAtDesc("goes-19", pageable))
                .thenReturn(new PageImpl<>(List.of(hotspot), pageable, 1));

        var page = service.findBySatellite("goes-19", 1);

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(21);
        assertThat(page.getContent().getFirst().getSatellite()).isEqualTo("GOES-19");
    }

    @Test
    void shouldFindHotspotsWithinRadius() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(0, 20);
        var hotspot = hotspot(
                UUID.randomUUID(),
                "focos_10min_20260707_0250.csv",
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        when(hotspotRepository.findWithinRadius(-26.9189, -49.0661, 10_000, pageable))
                .thenReturn(new PageImpl<>(List.of(hotspot), pageable, 1));

        var page = service.findWithinRadius(-26.9189, -49.0661, 10_000, 0);

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getLatitude()).isEqualTo(-26.9189);
        assertThat(page.getContent().getFirst().getLongitude()).isEqualTo(-49.0661);
    }

    @Test
    void shouldFindHotspotsWithinBoundingBox() {
        var service = new HotspotQueryService(hotspotRepository);
        var pageable = PageRequest.of(0, 20);
        var hotspot = hotspot(
                UUID.randomUUID(),
                "focos_10min_20260707_0250.csv",
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        when(hotspotRepository.findWithinBoundingBox(-27, -26, -50, -49, pageable))
                .thenReturn(new PageImpl<>(List.of(hotspot), pageable, 1));

        var page = service.findWithinBoundingBox(-27, -26, -50, -49, 0);

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getLatitude()).isEqualTo(-26.9189);
    }

    @Test
    void shouldCountHotspotsWithinRadiusSinceInstant() {
        var service = new HotspotQueryService(hotspotRepository);
        var observedSince = Instant.parse("2026-07-07T00:00:00Z");

        when(hotspotRepository.countWithinRadiusSince(-26.9189, -49.0661, 10_000, observedSince))
                .thenReturn(7L);

        long count = service.countWithinRadiusSince(-26.9189, -49.0661, 10_000, observedSince);

        assertThat(count).isEqualTo(7);
    }

    @Test
    void shouldCountHotspotsWithinBoundingBoxSinceInstant() {
        var service = new HotspotQueryService(hotspotRepository);
        var observedSince = Instant.parse("2026-07-07T00:00:00Z");

        when(hotspotRepository.countWithinBoundingBoxSince(-27, -26, -50, -49, observedSince))
                .thenReturn(4L);

        long count = service.countWithinBoundingBoxSince(-27, -26, -50, -49, observedSince);

        assertThat(count).isEqualTo(4);
    }

    private HotspotEntity hotspot(
            UUID id,
            String sourceFilename,
            double latitude,
            double longitude,
            String satellite,
            Instant observedAt
    ) {
        var hotspot = new HotspotEntity(
                sourceFilename,
                new InpeHotspot(latitude, longitude, satellite, observedAt)
        );
        ReflectionTestUtils.setField(hotspot, "id", id);

        return hotspot;
    }
}