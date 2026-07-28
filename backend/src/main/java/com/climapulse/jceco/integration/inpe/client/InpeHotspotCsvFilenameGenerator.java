package com.climapulse.jceco.integration.inpe.client;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class InpeHotspotCsvFilenameGenerator {

    private static final int RECENT_FILES_COUNT = 3;
    private static final int INPE_INTERVAL_MINUTES = 10;

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("'focos_10min_'yyyyMMdd_HHmm'.csv'")
                    .withZone(ZoneOffset.UTC);

    protected List<String> buildRecentFilenames() {
        List<String> filenames = new ArrayList<>();
        var roundedTime = roundDownToInpeInterval(Instant.now());

        for (int index = 0; index <= RECENT_FILES_COUNT - 1; index++) {
            int minutesToSubtract = index * INPE_INTERVAL_MINUTES;
            Instant instant = roundedTime.minus(Duration.ofMinutes(minutesToSubtract));

            filenames.add(FILENAME_FORMATTER.format(instant));
        }

        return filenames;
    }

    private Instant roundDownToInpeInterval(Instant instant) {
        var intervalSeconds = Duration.ofMinutes(10).toSeconds();
        var roundedEpochSecond = instant.getEpochSecond() / intervalSeconds * intervalSeconds;

        return Instant.ofEpochSecond(roundedEpochSecond);
    }
}