package com.climapulse.jceco.integration.inpe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;

@Service
public class InpeHotspotImporter {

    private final InpeHotspotCsvParser parser;
    private final InpeImportRepository inpeImportRepository;
    private final HotspotRepository hotspotRepository;

    public InpeHotspotImporter(
            InpeHotspotCsvParser parser,
            InpeImportRepository inpeImportRepository,
            HotspotRepository hotspotRepository
    ) {
        this.parser = parser;
        this.inpeImportRepository = inpeImportRepository;
        this.hotspotRepository = hotspotRepository;
    }

    @Transactional
    public InpeHotspotFileImportResult importFile(InpeCsvFile file) {
        if (inpeImportRepository.existsById(file.filename())) {
            return InpeHotspotFileImportResult.skipped(file.filename());
        }

        var hotspots = parser.parse(
                new StringReader(file.content())
        );

        inpeImportRepository.saveAndFlush(
                new InpeImportEntity(file.filename())
        );

        var hotspotEntities = hotspots.stream()
                .map(hotspot -> new HotspotEntity(
                        file.filename(),
                        hotspot
                ))
                .toList();

        hotspotRepository.saveAll(hotspotEntities);

        return InpeHotspotFileImportResult.imported(file.filename(), hotspotEntities.size());
    }
}
