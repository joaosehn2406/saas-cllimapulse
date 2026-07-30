package com.climapulse.jceco.integration.inpe.model;

public record InpeHotspotFileImportResult(
        String filename,
        boolean imported,
        boolean failed,
        int hotspotsSaved
) {

    public static InpeHotspotFileImportResult imported(String filename, int hotspotsSaved) {
        return new InpeHotspotFileImportResult(filename, true, false, hotspotsSaved);
    }

    public static InpeHotspotFileImportResult skipped(String filename) {
        return new InpeHotspotFileImportResult(filename, false, false, 0);
    }

    public static InpeHotspotFileImportResult failed(String filename) {
        return new InpeHotspotFileImportResult(filename, false, true, 0);
    }
}