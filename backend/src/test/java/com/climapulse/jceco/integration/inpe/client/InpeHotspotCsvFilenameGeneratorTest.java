package com.climapulse.jceco.integration.inpe.client;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class InpeHotspotCsvFilenameGeneratorTest {

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("'focos_10min_'yyyyMMdd_HHmm'.csv'");

    @Test
    void shouldBuildThreeRecentFilenames() {
        var generator = new InpeHotspotCsvFilenameGenerator();

        var filenames = generator.buildRecentFilenames();

        assertThat(filenames).hasSize(3);
        assertThat(filenames).allMatch(filename ->
                filename.matches("focos_10min_\\d{8}_\\d{4}\\.csv")
        );
    }

    @Test
    void shouldBuildFilenamesWithTenMinuteIntervals() {
        var generator = new InpeHotspotCsvFilenameGenerator();

        var filenames = generator.buildRecentFilenames();

        var firstFileTime = parseFilenameTime(filenames.getFirst());
        var secondFileTime = parseFilenameTime(filenames.get(1));
        var thirdFileTime = parseFilenameTime(filenames.getLast());

        assertThat(firstFileTime.getMinute() % 10).isZero();
        assertThat(secondFileTime).isEqualTo(firstFileTime.minusMinutes(10));
        assertThat(thirdFileTime).isEqualTo(secondFileTime.minusMinutes(10));
    }

    @Test
    void shouldBuildFilenamesBasedOnCurrentUtcTime() {
        var before = Instant.now();
        var generator = new InpeHotspotCsvFilenameGenerator();

        var filenames = generator.buildRecentFilenames();

        var after = Instant.now();
        var firstFileInstant = parseFilenameTime(filenames.getFirst()).toInstant(ZoneOffset.UTC);

        assertThat(firstFileInstant).isAfterOrEqualTo(before.minus(Duration.ofMinutes(10)));
        assertThat(firstFileInstant).isBeforeOrEqualTo(after);
    }

    private LocalDateTime parseFilenameTime(String filename) {
        return LocalDateTime.parse(filename, FILENAME_FORMATTER);
    }
}
