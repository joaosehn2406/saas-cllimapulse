package com.climapulse.jceco.integration.inpe.model;

import com.climapulse.jceco.shared.exception.InvalidCoordinateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class InpeHotspotTest {

    @Test
    void shouldCreateHotspotWhenCoordinatesAreValid() {
        var hotspot = new InpeHotspot(
                -26.9189,
                -49.0661,
                "GOES-19",
                Instant.parse("2026-07-07T02:50:00Z")
        );

        assertThat(hotspot.latitude()).isEqualTo(-26.9189);
        assertThat(hotspot.longitude()).isEqualTo(-49.0661);
        assertThat(hotspot.satellite()).isEqualTo("GOES-19");
    }

    @Test
    void shouldRejectInvalidLatitude() {
        assertThatThrownBy(() -> new InpeHotspot(
                200,
                -49.0661,
                "GOES-19",
                Instant.now()
        )).isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void shouldRejectInvalidLongitude() {
        assertThatThrownBy(() -> new InpeHotspot(
                -26.9189,
                -300,
                "GOES-19",
                Instant.now()
        )).isInstanceOf(InvalidCoordinateException.class);
    }
}