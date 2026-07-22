package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAiVisionResultLog;
import com.applicate.services.assetiq.entity.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiVisionResultLogRepository extends JpaRepository<AiqAiVisionResultLog, Long> {

    Optional<AiqAiVisionResultLog> findByTenantIdAndId(String tenantId, Long id);

    List<AiqAiVisionResultLog> findByTenantIdAndVisitCaptureId(String tenantId, Long visitCaptureId);

    List<AiqAiVisionResultLog> findByTenantIdAndAssetId(String tenantId, Long assetId);

    List<AiqAiVisionResultLog> findByTenantIdAndProcessingStatus(String tenantId, ProcessingStatus processingStatus);
}
