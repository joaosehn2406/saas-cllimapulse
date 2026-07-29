package com.climapulse.jceco.integration.inpe.importer;

public record InpeHotspotFileImportResult(
        String filename,
        boolean imported,
        boolean failed,
        int hotspotsSaved
) {

    static InpeHotspotFileImportResult imported(String filename, int hotspotsSaved) {
        return new InpeHotspotFileImportResult(filename, true, false, hotspotsSaved);
    }

    static InpeHotspotFileImportResult skipped(String filename) {
        return new InpeHotspotFileImportResult(filename, false, false, 0);
    }

    static InpeHotspotFileImportResult failed(String filename) {
        return new InpeHotspotFileImportResult(filename, false, true, 0);
    }
}