package com.climapulse.jceco.hotspot.service;

import com.climapulse.jceco.integration.inpe.persistence.HotspotEntity;
import com.climapulse.jceco.integration.inpe.persistence.HotspotRepository;
import com.climapulse.jceco.shared.pagination.PageRequestFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class HotspotQueryService {

    private final HotspotRepository hotspotRepository;
    private final PageRequestFactory pageRequestFactory;

    public HotspotQueryService(
            HotspotRepository hotspotRepository,
            PageRequestFactory pageRequestFactory
    ) {
        this.hotspotRepository = hotspotRepository;
        this.pageRequestFactory = pageRequestFactory;
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findRecent() {
        return findRecent(0);
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findRecent(int page) {
        return hotspotRepository.findAllByOrderByObservedAtDesc(pageRequestFactory.defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findByPeriod(Instant from, Instant to, int page) {
        return hotspotRepository.findByObservedAtBetweenOrderByObservedAtDesc(
                from,
                to,
                pageRequestFactory.defaultPage(page)
        );
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findBySatellite(String satellite, int page) {
        return hotspotRepository.findBySatelliteIgnoreCaseOrderByObservedAtDesc(
                satellite,
                pageRequestFactory.defaultPage(page)
        );
    }

    @Transactional(readOnly = true)
    public Page<HotspotEntity> findWithinRadius(
            double latitude,
            double longitude,
            double radiusMeters,
            int page
    ) {
        return hotspotRepository.findWithinRadius(latitude, longitude, radiusMeters, pageRequestFactory.defaultPage(page));
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
                pageRequestFactory.defaultPage(page)
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
}
