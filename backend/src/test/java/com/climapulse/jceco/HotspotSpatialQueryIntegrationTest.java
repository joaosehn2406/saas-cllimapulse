package com.climapulse.jceco;

import com.climapulse.jceco.hotspot.service.HotspotQueryService;
import com.climapulse.jceco.integration.inpe.model.InpeHotspotCsvFile;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.integration.inpe.persistence.InpeHotspotImportRepository;
import com.climapulse.jceco.integration.inpe.service.InpeHotspotCsvFileImporterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "climapulse.inpe.catch-up-enabled=false")
class HotspotSpatialQueryIntegrationTest {

    @Autowired
    private InpeHotspotCsvFileImporterService fileImporter;

    @Autowired
    private HotspotQueryService hotspotQueryService;

    @Autowired
    private HotspotRepository hotspotRepository;

    @Autowired
    private InpeHotspotImportRepository inpeHotspotImportRepository;

    @BeforeEach
    void cleanDatabase() {
        hotspotRepository.deleteAll();
        inpeHotspotImportRepository.deleteAll();
    }

    @Test
    void shouldFindOnlyHotspotsInsideRadius() {
        fileImporter.importFile(new InpeHotspotCsvFile("spatial-radius.csv", """
                lat,lon,satelite,data
                -26.918900,-49.066100,GOES-19,2026-07-07 02:50:00
                -12.881300,-68.021900,GOES-19,2026-07-07 02:55:00
                """));

        var page = hotspotQueryService.findWithinRadius(-26.9189, -49.0661, 5_000, 0);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getLatitude()).isEqualTo(-26.9189);
        assertThat(page.getContent().getFirst().getLongitude()).isEqualTo(-49.0661);
    }
}