package com.climapulse.jceco.hotspot.service;

import com.climapulse.jceco.integration.inpe.persistence.HotspotEntity;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class HotspotQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final HotspotRepository hotspotRepository;

    public HotspotQueryService(HotspotRepository hotspotRepository) {
        this.hotspotRepository = hotspotRepository;
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findRecent() {
        return findRecent(0);
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findRecent(int page) {
        return hotspotRepository.findAllByOrderByObservedAtDesc(defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findByPeriod(Instant from, Instant to, int page) {
        return hotspotRepository.findByObservedAtBetweenOrderByObservedAtDesc(from, to, defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findBySatellite(String satellite, int page) {
        return hotspotRepository.findBySatelliteIgnoreCaseOrderByObservedAtDesc(satellite, defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findWithinRadius(
            double latitude,
            double longitude,
            double radiusMeters,
            int page
    ) {
        return hotspotRepository.findWithinRadius(latitude, longitude, radiusMeters, defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findWithinBoundingBox(
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude,
            int page
    ) {
        return hotspotRepository.findWithinBoundingBox(
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude,
                defaultPage(page)
        );
    }

    @Transactional(readOnly = true)
    public long countWithinRadiusSince(
            double latitude,
            double longitude,
            double radiusMeters,
            Instant observedSince
    ) {
        return hotspotRepository.countWithinRadiusSince(latitude, longitude, radiusMeters, observedSince);
    }

    @Transactional(readOnly = true)
    public long countWithinBoundingBoxSince(
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude,
            Instant observedSince
    ) {
        return hotspotRepository.countWithinBoundingBoxSince(
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude,
                observedSince
        );
    }

    private PageRequest defaultPage(int page) {
        return PageRequest.of(page, DEFAULT_PAGE_SIZE);
    }
}