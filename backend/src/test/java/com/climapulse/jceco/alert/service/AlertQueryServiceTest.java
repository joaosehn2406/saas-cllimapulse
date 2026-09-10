package com.climapulse.jceco.alert.service;

import com.climapulse.jceco.alert.model.AlertSeverity;
import com.climapulse.jceco.alert.model.AlertStatus;
import com.climapulse.jceco.alert.persistence.AlertEntity;
import com.climapulse.jceco.alert.persistence.AlertRepository;
import com.climapulse.jceco.shared.pagination.PageRequestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertQueryServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private final PageRequestFactory pageRequestFactory = new PageRequestFactory();

    @Test
    void shouldFindRecentAlertsUsingDefaultPageSize() {
        var service = new AlertQueryService(alertRepository, pageRequestFactory);
        var pageable = PageRequest.of(0, 20);
        var alert = alert(AlertSeverity.CRITICAL);

        when(alertRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        var page = service.findRecent();

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getContent().getFirst()).isSameAs(alert);
    }

    @Test
    void shouldFindOpenAlertsUsingRequestedPage() {
        var service = new AlertQueryService(alertRepository, pageRequestFactory);
        var pageable = PageRequest.of(2, 20);

        when(alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 40));

        var page = service.findOpen(2);

        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void shouldFindAlertsBySeverity() {
        var service = new AlertQueryService(alertRepository, pageRequestFactory);
        var pageable = PageRequest.of(1, 20);
        var alert = alert(AlertSeverity.HIGH);

        when(alertRepository.findBySeverityOrderByCreatedAtDesc(AlertSeverity.HIGH, pageable))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        var page = service.findBySeverity(AlertSeverity.HIGH, 1);

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getContent().getFirst().getSeverity()).isEqualTo(AlertSeverity.HIGH);
    }

    private AlertEntity alert(AlertSeverity severity) {
        return new AlertEntity(
                UUID.fromString("0199187d-81a4-7d7a-9e66-df2f45e1d88a"),
                -26.9189,
                -49.0661,
                10_000,
                severity,
                AlertStatus.OPEN,
                "%s fire risk detected".formatted(severity.name()),
                "Fire risk score detected.",
                Instant.parse("2026-09-08T14:30:00Z"),
                null
        );
    }
}
