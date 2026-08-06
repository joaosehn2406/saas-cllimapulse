package com.climapulse.jceco.integration.inpe.job;

import com.climapulse.jceco.integration.inpe.service.InpeHotspotImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InpeHotspotLiveImportJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(InpeHotspotLiveImportJob.class);

    private final InpeHotspotImportService inpeHotspotImportService;

    public InpeHotspotLiveImportJob(InpeHotspotImportService inpeHotspotImportService) {
        this.inpeHotspotImportService = inpeHotspotImportService;
    }

    @Scheduled(cron = "${climapulse.inpe.live-import-cron}", zone = "UTC")
    public void importRecentHotspot() {
        var summary = inpeHotspotImportService.importRecentHotspot();

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
