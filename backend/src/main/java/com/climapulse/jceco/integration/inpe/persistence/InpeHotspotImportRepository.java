package com.climapulse.jceco.integration.inpe.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InpeHotspotImportRepository extends JpaRepository<InpeHotspotImportEntity, String> {
}