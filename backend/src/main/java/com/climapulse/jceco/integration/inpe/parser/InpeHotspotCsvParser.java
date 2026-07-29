package com.climapulse.jceco.integration.inpe.parser;

import com.climapulse.jceco.integration.inpe.model.InpeHotspot;
import com.climapulse.jceco.shared.exception.InpeCsvParsingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Component
public class InpeHotspotCsvParser {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "lat",
            "lon",
            "satelite",
            "data"
    );

    private static final DateTimeFormatter INPE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter INPE_DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    public List<InpeHotspot> parse(Reader reader) {
        try (var parser = createParser(reader)) {
            validateHeader(parser);

            return parser.stream()
                    .map(record -> new InpeHotspot(
                            parseLatitude(record.get("lat")),
                            parseLongitude(record.get("lon")),
                            record.get("satelite"),
                            parseObservedAt(record.get("data"))
                    )).toList();
        } catch (IOException exception) {
            throw new InpeCsvParsingException("Could not read INPE CSV", exception);
        }
    }

    private void validateHeader(CSVParser parser) {
        var header = parser.getHeaderMap().keySet();

        if (!header.containsAll(REQUIRED_HEADERS)) {
            throw new InpeCsvParsingException(
                    "Invalid INPE CSV headers. Required headers: " + REQUIRED_HEADERS
            );
        }
    }

    private CSVParser createParser(Reader reader) throws IOException {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get()
                .parse(reader);
    }

    private double parseLongitude(String longitude) {
        return parseDouble(longitude, "lon");
    }

    private double parseLatitude(String latitude) {
        return parseDouble(latitude, "lat");
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new InpeCsvParsingException("Invalid numeric value for field: " + fieldName, exception);
        }
    }

    private Instant parseObservedAt(String value) {
        try {
            if (value.length() == 10) {
                return LocalDate.parse(value, INPE_DATE_FORMATTER)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC)
                        .toInstant();
            }

            return LocalDateTime.parse(value, INPE_DATE_TIME_FORMATTER)
                    .atOffset(ZoneOffset.UTC)
                    .toInstant();
        } catch (RuntimeException exception) {
            throw new InpeCsvParsingException("Invalid INPE date: " + value, exception);
        }
    }
}