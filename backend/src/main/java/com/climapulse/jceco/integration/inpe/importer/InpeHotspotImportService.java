package com.climapulse.jceco.integration.inpe.importer;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InpeHotspotImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InpeHotspotImportService.class);

    private final InpeHotspotFileImporterService fileImporter;
    private final InpeHotspotCsvClient csvClient;

    public InpeHotspotImportService(
            InpeHotspotFileImporterService fileImporter,
            InpeHotspotCsvClient csvClient
    ) {
        this.fileImporter = fileImporter;
        this.csvClient = csvClient;
    }

    public InpeHotspotImportSummary importRecentHotspots() {
        var csvFiles = csvClient.fetchRecentCsvs();
        var results = new ArrayList<InpeHotspotFileImportResult>();

        for (var csvFile : csvFiles) {
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