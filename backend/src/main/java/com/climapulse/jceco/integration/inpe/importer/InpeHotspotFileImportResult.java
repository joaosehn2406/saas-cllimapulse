package com.climapulse.jceco.integration.inpe.importer;

public record InpeHotspotFileImportResult(
        String filename,
        boolean imported,
        int hotspotsSaved
) {

    static InpeHotspotFileImportResult imported(String filename, int hotspotsSaved) {
        return new InpeHotspotFileImportResult(filename, true, hotspotsSaved);
    }

    static InpeHotspotFileImportResult skipped(String filename) {
        return new InpeHotspotFileImportResult(filename, false, 0);
    }
}