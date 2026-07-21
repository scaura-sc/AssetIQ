package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAiVisionResultLog;
import com.applicate.services.assetiq.entity.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiVisionResultLogRepository extends JpaRepository<AiqAiVisionResultLog, Long> {

    List<AiqAiVisionResultLog> findByTenantIdAndVisitCaptureId(String tenantId, Long visitCaptureId);

    List<AiqAiVisionResultLog> findByTenantIdAndAssetId(String tenantId, Long assetId);

    List<AiqAiVisionResultLog> findByTenantIdAndProcessingStatus(String tenantId, ProcessingStatus processingStatus);
}
