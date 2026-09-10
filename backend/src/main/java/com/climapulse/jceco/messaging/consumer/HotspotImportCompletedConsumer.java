package com.climapulse.jceco.messaging.consumer;

import com.climapulse.jceco.alert.service.AlertService;
import com.climapulse.jceco.messaging.event.HotspotImportCompletedEvent;
import com.climapulse.jceco.risk.config.RiskProperties;
import com.climapulse.jceco.risk.persistence.RiskAssessmentEntity;
import com.climapulse.jceco.risk.service.RiskAssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HotspotImportCompletedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotspotImportCompletedConsumer.class);

    private final RiskAssessmentService riskAssessmentService;
    private final AlertService alertService;
    private final RiskProperties riskProperties;

    public HotspotImportCompletedConsumer(
            RiskAssessmentService riskAssessmentService,
            AlertService alertService,
            RiskProperties riskProperties
    ) {
        this.riskAssessmentService = riskAssessmentService;
        this.alertService = alertService;
        this.riskProperties = riskProperties;
    }

    @KafkaListener(topics = "${climapulse.kafka.hotspot-import-completed-topic}")
    public void consume(String payload) {
        var event = HotspotImportCompletedEvent.fromPayload(payload);

        if (event.isEmpty()) {
            LOGGER.warn("Could not read hotspot import event payload");
            return;
        }

        if (event.get().hotspotsSaved() == 0) {
            return;
        }

        for (RiskProperties.MonitoredArea area : riskProperties.monitoredAreas()) {
            processArea(area);
        }
    }

    private void processArea(RiskProperties.MonitoredArea area) {
        try {
            RiskAssessmentEntity assessment = riskAssessmentService.calculateAndSave(
                    area.latitude(),
                    area.longitude(),
                    area.radiusMeters()
            );

            alertService.createOpenAlertIfNeeded(assessment);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not process risk assessment for monitored area: {}", area.name(), exception);
        }
    }
}
