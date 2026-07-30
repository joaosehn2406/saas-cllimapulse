package com.climapulse.jceco.integration.inpe.importer;

import com.climapulse.jceco.integration.inpe.model.InpeHotspotFileImportResult;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotImportSummary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InpeHotspotImportSummaryTest {

    @Test
    void shouldSummarizeImportedSkippedAndFailedFilesSeparately() {
        var results = List.of(
                InpeHotspotFileImportResult.imported("imported.csv", 2),
                InpeHotspotFileImportResult.skipped("skipped.csv"),
                InpeHotspotFileImportResult.failed("failed.csv")
        );

        var summary = InpeHotspotImportSummary.from(results);

        assertThat(summary.filesFound()).isEqualTo(3);
        assertThat(summary.filesImported()).isEqualTo(1);
        assertThat(summary.filesSkipped()).isEqualTo(1);
        assertThat(summary.failedFiles()).isEqualTo(1);
        assertThat(summary.hotspotsSaved()).isEqualTo(2);
    }
}