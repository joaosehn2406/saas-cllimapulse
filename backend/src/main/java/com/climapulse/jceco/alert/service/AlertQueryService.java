package com.climapulse.jceco.alert.service;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.alert.persistence.AlertRepository;
import com.climapulse.jceco.shared.pagination.PageRequestFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertQueryService {

    private final AlertRepository alertRepository;
    private final PageRequestFactory pageRequestFactory;

    public AlertQueryService(
            AlertRepository alertRepository,
            PageRequestFactory pageRequestFactory
    ) {
        this.alertRepository = alertRepository;
        this.pageRequestFactory = pageRequestFactory;
    }

    @Transactional(readOnly = true)
    public Page<AlertEntity> findRecent() {
        return findRecent(0);
    }

    @Transactional(readOnly = true)
    public Page<AlertEntity> findRecent(int page) {
        return alertRepository.findAllByOrderByCreatedAtDesc(pageRequestFactory.defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<AlertEntity> findOpen(int page) {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN, pageRequestFactory.defaultPage(page));
    }

    @Transactional(readOnly = true)
    public Page<AlertEntity> findBySeverity(AlertSeverity severity, int page) {
        return alertRepository.findBySeverityOrderByCreatedAtDesc(severity, pageRequestFactory.defaultPage(page));
    }
}
