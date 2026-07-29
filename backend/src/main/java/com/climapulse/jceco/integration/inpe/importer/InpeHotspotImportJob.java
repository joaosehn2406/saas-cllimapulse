package com.climapulse.jceco.integration.inpe.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InpeHotspotImportJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(InpeHotspotImportJob.class);

    private final InpeHotspotImportService inpeHotspotImportService;

    public InpeHotspotImportJob(InpeHotspotImportService inpeHotspotImportService) {
        this.inpeHotspotImportService = inpeHotspotImportService;
    }

    @Scheduled(fixedDelayString = "${climapulse.inpe.import-delay}")
    public void importRecentHotspots() {
        var summary = inpeHotspotImportService.importRecentHotspots();

        LOGGER.info(
                "INPE hotspot import finished. filesFound={}, filesImported={}, filesSkipped={}, failedFiles={}, hotspotsSaved={}",
                summary.filesFound(),
                summary.filesImported(),
                summary.filesSkipped(),
                summary.failedFiles(),
                summary.hotspotsSaved()
        );
    }
}