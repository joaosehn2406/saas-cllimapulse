package com.climapulse.jceco.integration.inpe.importer;

import java.util.List;

public record InpeHotspotImportSummary(
        int filesFound,
        int filesImported,
        int filesSkipped,
        int hotspotsSaved
) {

    static InpeHotspotImportSummary from(List<InpeHotspotFileImportResult> results) {
        int filesImported = 0;
        int hotspotsSaved = 0;

        for (var result : results) {
            if (result.imported()) {
                filesImported++;
            }

            hotspotsSaved += result.hotspotsSaved();
        }

        return new InpeHotspotImportSummary(
                results.size(),
                filesImported,
                results.size() - filesImported,
                hotspotsSaved
        );
    }
}