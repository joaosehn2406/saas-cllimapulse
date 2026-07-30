package com.climapulse.jceco.integration.inpe.client;

import com.climapulse.jceco.integration.inpe.config.InpeProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class InpeHotspotCsvFilenameGenerator {

    private static final int INPE_INTERVAL_MINUTES = 10;

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("'focos_10min_'yyyyMMdd_HHmm'.csv'")
                    .withZone(ZoneOffset.UTC);

    private final int recentFilesCount;

    public InpeHotspotCsvFilenameGenerator(InpeProperties inpeProperties) {
        this.recentFilesCount = inpeProperties.recentFilesCount();
    }

    public String buildRecentFilename() {

        return FILENAME_FORMATTER.format(roundDownToInpeInterval(Instant.now()));
    }

    protected List<String> buildRecentFilenames() {
        List<String> filenames = new ArrayList<>();
        Instant currentTimeRounded = roundDownToInpeInterval(Instant.now());

        for (int index = 0; index < recentFilesCount; index++) {
            int minutesToSubtract = index * INPE_INTERVAL_MINUTES;
            Instant instant = currentTimeRounded.minus(Duration.ofMinutes(minutesToSubtract));

            filenames.add(FILENAME_FORMATTER.format(instant));
        }

        return filenames;
    }

    private Instant roundDownToInpeInterval(Instant instant) {
        long intervalSeconds = Duration.ofMinutes(INPE_INTERVAL_MINUTES).toSeconds();
        long roundedEpochSecond = instant.getEpochSecond() / intervalSeconds * intervalSeconds;

        return Instant.ofEpochSecond(roundedEpochSecond);
    }
}