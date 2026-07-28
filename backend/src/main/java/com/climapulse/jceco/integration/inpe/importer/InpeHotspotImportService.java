package com.climapulse.jceco.integration.inpe.importer;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InpeHotspotImportService {

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
            results.add(fileImporter.importFile(csvFile));
        }

        return InpeHotspotImportSummary.from(results);
    }
}