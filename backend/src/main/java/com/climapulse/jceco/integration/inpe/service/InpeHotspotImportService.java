package com.climapulse.jceco.integration.inpe.service;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvClient;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotCsvFile;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotFileImportResult;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InpeHotspotImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InpeHotspotImportService.class);

    private final InpeHotspotCsvFileImporterService fileImporter;
    private final InpeHotspotCsvClient csvClient;

    public InpeHotspotImportService(
            InpeHotspotCsvFileImporterService fileImporter,
            InpeHotspotCsvClient csvClient
    ) {
        this.fileImporter = fileImporter;
        this.csvClient = csvClient;
    }

    public InpeHotspotImportSummary importRecentHotspot() {
        Optional<InpeHotspotCsvFile> csvFile = csvClient.fetchRecentCsv();

        if (csvFile.isEmpty()) {
            return InpeHotspotImportSummary.from(List.of());
        }

        InpeHotspotCsvFile file = csvFile.get();

        try {
            InpeHotspotFileImportResult result = fileImporter.importFile(file);

            return InpeHotspotImportSummary.from(List.of(result));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not import INPE file: {}", file.filename(), exception);

            return InpeHotspotImportSummary.from(List.of(
                    InpeHotspotFileImportResult.failed(file.filename())
            ));
        }
    }

    public InpeHotspotImportSummary importRecentHotspots() {
        List<InpeHotspotCsvFile> csvFiles = csvClient.fetchRecentCsvs();
        List<InpeHotspotFileImportResult> results = new ArrayList<>();

        for (InpeHotspotCsvFile csvFile : csvFiles) {
            try {
                results.add(fileImporter.importFile(csvFile));
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not import INPE file: {}", csvFile.filename(), exception);
                results.add(InpeHotspotFileImportResult.failed(csvFile.filename()));
            }
        }

        return InpeHotspotImportSummary.from(results);
    }
}