package com.climapulse.jceco.integration.inpe.importer;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvClient;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotCsvFile;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotFileImportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InpeHotspotImportServiceTest {

    @Mock
    private InpeHotspotCsvFileImporterService fileImporter;

    @Mock
    private InpeHotspotCsvClient csvClient;

    @Test
    void shouldContinueImportingWhenOneFileFails() {
        var service = new InpeHotspotImportService(fileImporter, csvClient);
        var importedFile = new InpeHotspotCsvFile("imported.csv", "csv-content");
        var failedFile = new InpeHotspotCsvFile("failed.csv", "bad-csv-content");

        when(csvClient.fetchRecentCsvs()).thenReturn(List.of(importedFile, failedFile));
        when(fileImporter.importFile(importedFile)).thenReturn(InpeHotspotFileImportResult.imported(importedFile.filename(), 2));
        when(fileImporter.importFile(failedFile)).thenThrow(new RuntimeException("Invalid CSV"));

        var summary = service.importRecentHotspots();

        assertThat(summary.filesFound()).isEqualTo(2);
        assertThat(summary.filesImported()).isEqualTo(1);
        assertThat(summary.filesSkipped()).isZero();
        assertThat(summary.failedFiles()).isEqualTo(1);
        assertThat(summary.hotspotsSaved()).isEqualTo(2);
    }
}