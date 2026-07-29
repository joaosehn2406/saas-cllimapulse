package com.climapulse.jceco.integration.inpe.client;

import com.climapulse.jceco.integration.inpe.config.InpeProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
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
    void shouldBuildConfiguredAmountOfRecentFilenames() {
        var generator = generatorWithRecentFilesCount(5);

        var filenames = generator.buildRecentFilenames();

        assertThat(filenames).hasSize(5);
        assertThat(filenames).allMatch(filename ->
                filename.matches("focos_10min_\\d{8}_\\d{4}\\.csv")
        );
    }

    @Test
    void shouldBuildFilenamesWithTenMinuteIntervals() {
        var generator = generatorWithRecentFilesCount(3);

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
        var generator = generatorWithRecentFilesCount(3);

        var filenames = generator.buildRecentFilenames();

        var after = Instant.now();
        var firstFileInstant = parseFilenameTime(filenames.getFirst()).toInstant(ZoneOffset.UTC);

        assertThat(firstFileInstant).isAfterOrEqualTo(before.minus(Duration.ofMinutes(10)));
        assertThat(firstFileInstant).isBeforeOrEqualTo(after);
    }

    private InpeHotspotCsvFilenameGenerator generatorWithRecentFilesCount(int recentFilesCount) {
        var properties = new InpeProperties(URI.create("https://example.com/"), recentFilesCount);

        return new InpeHotspotCsvFilenameGenerator(properties);
    }

    private LocalDateTime parseFilenameTime(String filename) {
        return LocalDateTime.parse(filename, FILENAME_FORMATTER);
    }
}