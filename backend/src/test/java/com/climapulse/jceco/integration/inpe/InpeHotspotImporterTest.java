package com.climapulse.jceco.integration.inpe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Reader;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InpeHotspotImporterTest {

    @Mock
    private InpeHotspotCsvParser parser;

    @Mock
    private InpeImportRepository inpeImportRepository;

    @Mock
    private HotspotRepository hotspotRepository;

    @Test
    void shouldSkipAlreadyImportedFile() {
        var importer = new InpeHotspotImporter(parser, inpeImportRepository, hotspotRepository);
        var file = new InpeCsvFile("focos_10min_20260727_1200.csv", "lat,lon,satelite,data\n");

        when(inpeImportRepository.existsById(file.filename())).thenReturn(true);

        var result = importer.importFile(file);

        assertThat(result.filename()).isEqualTo(file.filename());
        assertThat(result.imported()).isFalse();
        assertThat(result.hotspotsSaved()).isZero();

        verify(inpeImportRepository, never()).saveAndFlush(any());
        verifyNoInteractions(parser, hotspotRepository);
    }

    @Test
    void shouldImportNewFileAndPersistParsedHotspots() {
        var importer = new InpeHotspotImporter(parser, inpeImportRepository, hotspotRepository);
        var file = new InpeCsvFile("focos_10min_20260727_1200.csv", "csv-content");
        var hotspots = List.of(
                new InpeHotspot(-26.918900, -49.066100, "GOES-19", Instant.parse("2026-07-27T12:00:00Z")),
                new InpeHotspot(-12.881300, -68.021900, "GOES-19", Instant.parse("2026-07-27T12:00:00Z"))
        );

        when(inpeImportRepository.existsById(file.filename())).thenReturn(false);
        when(parser.parse(any(Reader.class))).thenReturn(hotspots);

        var result = importer.importFile(file);

        assertThat(result.filename()).isEqualTo(file.filename());
        assertThat(result.imported()).isTrue();
        assertThat(result.hotspotsSaved()).isEqualTo(2);

        verify(inpeImportRepository).saveAndFlush(any(InpeImportEntity.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<HotspotEntity>> hotspotsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(hotspotRepository).saveAll(hotspotsCaptor.capture());
        assertThat(hotspotsCaptor.getValue()).hasSize(2);
    }
}
