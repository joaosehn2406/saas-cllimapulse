package com.climapulse.jceco;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvClient;
import com.climapulse.jceco.integration.inpe.service.InpeHotspotCsvFileImporterService;
import com.climapulse.jceco.integration.inpe.service.InpeHotspotImportService;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotCsvFile;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.integration.inpe.persistence.InpeHotspotImportRepository;
import com.climapulse.jceco.shared.exception.InpeCsvParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "climapulse.inpe.catch-up-enabled=false")
class InpeHotspotImportFlowIntegrationTest {

    @Autowired
    private InpeHotspotCsvFileImporterService fileImporter;

    @Autowired
    private InpeHotspotImportService importService;

    @Autowired
    private InpeHotspotImportRepository inpeHotspotImportRepository;

    @Autowired
    private HotspotRepository hotspotRepository;

    @MockitoBean
    private InpeHotspotCsvClient csvClient;

    @BeforeEach
    void cleanDatabase() {
        hotspotRepository.deleteAll();
        inpeHotspotImportRepository.deleteAll();
    }

    @Test
    void shouldNotRegisterFileWhenCsvIsInvalid() {
        var file = invalidFile("invalid-not-registered.csv");

        assertThatThrownBy(() -> fileImporter.importFile(file))
                .isInstanceOf(InpeCsvParsingException.class);

        assertThat(inpeHotspotImportRepository.existsById(file.filename())).isFalse();
    }

    @Test
    void shouldNotPersistAnyHotspotWhenCsvIsInvalid() {
        var file = invalidFile("invalid-no-hotspots.csv");

        assertThatThrownBy(() -> fileImporter.importFile(file))
                .isInstanceOf(InpeCsvParsingException.class);

        assertThat(hotspotRepository.count()).isZero();
    }

    @Test
    void shouldContinueWithNextFileWhenPreviousCsvIsInvalid() {
        var invalidFile = invalidFile("invalid-first.csv");
        var validFile = validFile("valid-second.csv");

        when(csvClient.fetchRecentCsvs()).thenReturn(List.of(invalidFile, validFile));

        var summary = importService.importRecentHotspots();

        assertThat(summary.filesFound()).isEqualTo(2);
        assertThat(summary.failedFiles()).isEqualTo(1);
        assertThat(summary.filesImported()).isEqualTo(1);
        assertThat(summary.hotspotsSaved()).isEqualTo(2);
        assertThat(inpeHotspotImportRepository.existsById(invalidFile.filename())).isFalse();
        assertThat(inpeHotspotImportRepository.existsById(validFile.filename())).isTrue();
        assertThat(hotspotRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldSkipSameValidFileOnSecondImport() {
        var file = validFile("idempotent-skipped.csv");

        var firstResult = fileImporter.importFile(file);
        var secondResult = fileImporter.importFile(file);

        assertThat(firstResult.imported()).isTrue();
        assertThat(firstResult.hotspotsSaved()).isEqualTo(2);
        assertThat(secondResult.imported()).isFalse();
        assertThat(secondResult.hotspotsSaved()).isZero();
    }

    @Test
    void shouldNotDuplicateHotspotsWhenSameFileIsImportedAgain() {
        var file = validFile("idempotent-no-duplicates.csv");

        fileImporter.importFile(file);
        var countAfterFirstImport = hotspotRepository.count();

        fileImporter.importFile(file);
        var countAfterSecondImport = hotspotRepository.count();

        assertThat(countAfterFirstImport).isEqualTo(2);
        assertThat(countAfterSecondImport).isEqualTo(countAfterFirstImport);
    }

    private InpeHotspotCsvFile invalidFile(String filename) {
        return new InpeHotspotCsvFile(filename, """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                invalid-latitude,-48.000000,GOES-19,2026-07-07 03:00:00
                """);
    }

    private InpeHotspotCsvFile validFile(String filename) {
        return new InpeHotspotCsvFile(filename, """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                -12.881300,-68.021900,GOES-19,2026-07-07 02:55:00
                """);
    }
}
