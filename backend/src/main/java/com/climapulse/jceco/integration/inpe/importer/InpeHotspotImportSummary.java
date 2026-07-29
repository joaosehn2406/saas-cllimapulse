package com.climapulse.jceco.integration.inpe.importer;

import java.util.List;

public record InpeHotspotImportSummary(
        int filesFound,
        int filesImported,
        int filesSkipped,
        int failedFiles,
        int hotspotsSaved
) {

    static InpeHotspotImportSummary from(List<InpeHotspotFileImportResult> results) {
        int filesFound = results.size();
        int filesImported = 0;
        int hotspotsSaved = 0;
        int failedFiles = 0;

        for (var result : results) {
            if (result.imported()) {
                filesImported++;
            }

            if (result.failed()) {
                failedFiles++;
            }

            hotspotsSaved += result.hotspotsSaved();
        }

        int filesSkipped = filesFound - filesImported - failedFiles;

        return new InpeHotspotImportSummary(
                filesFound,
                filesImported,
                filesSkipped,
                failedFiles,
                hotspotsSaved
        );
    }
}