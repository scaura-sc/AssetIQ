package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAiVisionResultLog;
import com.applicate.services.assetiq.entity.enums.ProcessingStatus;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiVisionResultLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AiVisionResultLogRepository aiVisionResultLogRepository;

    @Test
    void persistsPendingResultThenReflectsCompletion() {
        AiqAiVisionResultLog result = new AiqAiVisionResultLog();
        result.setTenantId("tenant-1");
        result.setVisitCaptureId(5001L);
        result.setAssetId(1001L);
        result.setPhotoUrl("https://example.com/photo1.jpg");
        result.setModelVersion("v1.3.0");

        AiqAiVisionResultLog saved = aiVisionResultLogRepository.saveAndFlush(result);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProcessingStatus()).isEqualTo(ProcessingStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();

        saved.setProcessingStatus(ProcessingStatus.SUCCESS);
        saved.setDetectedPurityPct(new BigDecimal("91.00"));
        saved.setDetectedCondition("GOOD");
        saved.setConfidenceScore(new BigDecimal("87.50"));
        saved.setRawResult("{\"purity\":91.0,\"condition\":\"GOOD\"}");
        saved.setProcessedAt(LocalDateTime.of(2025, 6, 1, 10, 5));
        AiqAiVisionResultLog updated = aiVisionResultLogRepository.saveAndFlush(saved);

        assertThat(updated.getProcessingStatus()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(updated.getDetectedPurityPct()).isEqualByComparingTo("91.00");
    }

    @Test
    void findsByVisitCaptureAssetAndStatus() {
        AiqAiVisionResultLog result = new AiqAiVisionResultLog();
        result.setTenantId("tenant-2");
        result.setVisitCaptureId(6002L);
        result.setAssetId(2002L);
        result.setPhotoUrl("https://example.com/photo2.jpg");
        result.setModelVersion("v1.3.0");
        aiVisionResultLogRepository.saveAndFlush(result);

        List<AiqAiVisionResultLog> byCapture = aiVisionResultLogRepository.findByTenantIdAndVisitCaptureId("tenant-2", 6002L);
        assertThat(byCapture).hasSize(1);

        List<AiqAiVisionResultLog> byAsset = aiVisionResultLogRepository.findByTenantIdAndAssetId("tenant-2", 2002L);
        assertThat(byAsset).hasSize(1);

        List<AiqAiVisionResultLog> pending =
                aiVisionResultLogRepository.findByTenantIdAndProcessingStatus("tenant-2", ProcessingStatus.PENDING);
        assertThat(pending).hasSize(1);
    }
}
