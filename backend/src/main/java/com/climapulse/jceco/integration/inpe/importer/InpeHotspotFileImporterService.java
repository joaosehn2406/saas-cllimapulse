package com.climapulse.jceco.integration.inpe.importer;

import com.climapulse.jceco.integration.inpe.client.InpeHotspotCsvFile;
import com.climapulse.jceco.integration.inpe.parser.InpeHotspotCsvParser;
import com.climapulse.jceco.integration.inpe.persistence.HotspotEntity;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.integration.inpe.persistence.InpeHotspotImportEntity;
import com.climapulse.jceco.integration.inpe.persistence.InpeHotspotImportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;

@Service
public class InpeHotspotFileImporterService {

    private final InpeHotspotCsvParser parser;
    private final InpeHotspotImportRepository inpeHotspotImportRepository;
    private final HotspotRepository hotspotRepository;

    public InpeHotspotFileImporterService(
            InpeHotspotCsvParser parser,
            InpeHotspotImportRepository inpeHotspotImportRepository,
            HotspotRepository hotspotRepository
    ) {
        this.parser = parser;
        this.inpeHotspotImportRepository = inpeHotspotImportRepository;
        this.hotspotRepository = hotspotRepository;
    }

    @Transactional
    public InpeHotspotFileImportResult importFile(InpeHotspotCsvFile file) {
        if (inpeHotspotImportRepository.existsById(file.filename())) {
            return InpeHotspotFileImportResult.skipped(file.filename());
        }

        var hotspots = parser.parse(
                new StringReader(file.content())
        );

        inpeHotspotImportRepository.saveAndFlush(
                new InpeHotspotImportEntity(file.filename())
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