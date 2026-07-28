package com.climapulse.jceco.integration.inpe;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InpeHotspotService {

    private final InpeHotspotImporter importer;
    private final InpeHotspotCsvClient csvClient;

    public InpeHotspotService(
            InpeHotspotImporter importer,
            InpeHotspotCsvClient csvClient
    ) {
        this.importer = importer;
        this.csvClient = csvClient;
    }

    public InpeHotspotImportSummary importRecentHotspots() {
        var csvFiles = csvClient.fetchRecentCsvs();
        var results = new ArrayList<InpeHotspotFileImportResult>();

        for (var csvFile : csvFiles) {
            results.add(importer.importFile(csvFile));
        }

        return InpeHotspotImportSummary.from(results);
    }
}
