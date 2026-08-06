package com.climapulse.jceco.integration.inpe.job;

import com.climapulse.jceco.integration.inpe.service.InpeHotspotImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "climapulse.inpe", name = "catch-up-enabled", havingValue = "true")
public class InpeHotspotCatchUpJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(InpeHotspotCatchUpJob.class);

    private final InpeHotspotImportService inpeHotspotImportService;

    public InpeHotspotCatchUpJob(InpeHotspotImportService inpeHotspotImportService) {
        this.inpeHotspotImportService = inpeHotspotImportService;
    }

    @EventListener(ApplicationReadyEvent.class)
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