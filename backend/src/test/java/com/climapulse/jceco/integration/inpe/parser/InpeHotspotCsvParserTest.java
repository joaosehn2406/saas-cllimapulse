package com.climapulse.jceco.integration.inpe.parser;

import com.climapulse.jceco.integration.inpe.model.InpeHotspot;
import com.climapulse.jceco.shared.exception.InpeCsvParsingException;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InpeHotspotCsvParserTest {

    private final InpeHotspotCsvParser parser = new InpeHotspotCsvParser();

    @Test
    void shouldParseValidInpeCsv() {
        var csv = """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                """;

        var hotspots = parser.parse(new StringReader(csv));

        assertThat(hotspots).hasSize(1);

        var hotspot = hotspots.getFirst();
        assertThat(hotspot.latitude()).isEqualTo(-26.918900);
        assertThat(hotspot.longitude()).isEqualTo(-49.066100);
        assertThat(hotspot.satellite()).isEqualTo("GOES-19");
        assertThat(hotspot.observedAt()).isEqualTo(Instant.parse("2026-07-07T02:50:00Z"));
    }

    @Test
    void shouldParseMultipleRows() {
        var csv = """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                -12.881300,-68.021900,GOES-19,2026-07-07 02:55:00
                """;

        var hotspots = parser.parse(new StringReader(csv));

        assertThat(hotspots).hasSize(2);
        assertThat(hotspots)
                .extracting(InpeHotspot::satellite)
                .containsExactly("GOES-19", "GOES-19");
    }

    @Test
    void shouldTrimValuesBeforeParsing() {
        var csv = """
                lat,lon,satelite,data
                 -26.918900, -49.066100, GOES-19, 2026-07-07 02:50:00
                """;

        var hotspots = parser.parse(new StringReader(csv));

        assertThat(hotspots).hasSize(1);
        assertThat(hotspots.getFirst().latitude()).isEqualTo(-26.918900);
        assertThat(hotspots.getFirst().longitude()).isEqualTo(-49.066100);
        assertThat(hotspots.getFirst().satellite()).isEqualTo("GOES-19");
    }

    @Test
    void shouldParseDateOnlyInpeCsvAsStartOfDayUtc() {
        var csv = """
                lat,lon,satelite,data
                -11.479000,-66.325300,GOES-19,2026-07-29
                """;

        var hotspots = parser.parse(new StringReader(csv));

        assertThat(hotspots).hasSize(1);
        assertThat(hotspots.getFirst().observedAt()).isEqualTo(Instant.parse("2026-07-29T00:00:00Z"));
    }
    @Test
    void shouldRejectCsvWithInvalidHeaders() {
        var csv = """
                latitude,longitude,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(InpeCsvParsingException.class)
                .hasMessageContaining("Invalid INPE CSV headers");
    }

    @Test
    void shouldRejectInvalidLatitudeValue() {
        var csv = """
                lat,lon,satelite,data
                abc,-49.066100,GOES-19,2026-07-07 02:50:00
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(InpeCsvParsingException.class)
                .hasMessageContaining("Invalid numeric value for field: lat");
    }

    @Test
    void shouldRejectInvalidDateValue() {
        var csv = """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,07/07/2026 02:50:00
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(InpeCsvParsingException.class)
                .hasMessageContaining("Invalid INPE date");
    }

    @Test
    void shouldRejectInvalidRow() {
        var csv = """
                lat,lon,satelite,data
                -26.918900,invalid-longitude,GOES-19,2026-07-07 02:50:00
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(InpeCsvParsingException.class)
                .hasMessageContaining("Invalid numeric value for field: lon");
    }

    @Test
    void shouldRejectWholeCsvWhenInvalidRowIsBetweenValidRows() {
        var csv = """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                invalid-latitude,-48.000000,GOES-19,2026-07-07 03:00:00
                -25.000000,-47.000000,GOES-19,2026-07-07 03:10:00
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(InpeCsvParsingException.class)
                .hasMessageContaining("Invalid numeric value for field: lat");
    }
}
