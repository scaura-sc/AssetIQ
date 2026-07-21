package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.DetectionSource;
import com.applicate.services.assetiq.entity.enums.PresenceStatus;
import com.applicate.services.assetiq.entity.enums.ScanMethod;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisitAssetCaptureRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private VisitAssetCaptureRepository visitAssetCaptureRepository;

    @Test
    void persistsCaptureWithDefaultsAndAiConfidence() {
        AiqVisitAssetCapture capture = new AiqVisitAssetCapture();
        capture.setTenantId("tenant-1");
        capture.setVisitId("VISIT-001");
        capture.setVisitDate(LocalDate.of(2025, 3, 1));
        capture.setOutletCode("OUT-100");
        capture.setSalesmanCode("SM-1");
        capture.setAssetId(1001L);
        capture.setAssetNumber("AST-0001");
        capture.setPresenceStatus(PresenceStatus.PRESENT);
        capture.setScanMethod(ScanMethod.QR);
        capture.setPuritySource(DetectionSource.AI_VISION);
        capture.setPurityPct(new BigDecimal("92.50"));
        capture.setPurityAiConfidence(new BigDecimal("88.00"));
        capture.setCapturedAt(LocalDateTime.of(2025, 3, 1, 11, 30));

        AiqVisitAssetCapture saved = visitAssetCaptureRepository.saveAndFlush(capture);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsPlannedVisit()).isFalse();
        assertThat(saved.getConditionSource()).isEqualTo(DetectionSource.MANUAL);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findsByAssetVisitAndOutlet() {
        AiqVisitAssetCapture capture = new AiqVisitAssetCapture();
        capture.setTenantId("tenant-2");
        capture.setVisitId("VISIT-002");
        capture.setVisitDate(LocalDate.of(2025, 3, 2));
        capture.setOutletCode("OUT-200");
        capture.setSalesmanCode("SM-2");
        capture.setAssetId(2002L);
        capture.setAssetNumber("AST-0002");
        capture.setPresenceStatus(PresenceStatus.NOT_FOUND);
        capture.setCapturedAt(LocalDateTime.of(2025, 3, 2, 9, 0));
        visitAssetCaptureRepository.saveAndFlush(capture);

        List<AiqVisitAssetCapture> byAsset =
                visitAssetCaptureRepository.findByTenantIdAndAssetIdOrderByCapturedAtDesc("tenant-2", 2002L);
        assertThat(byAsset).hasSize(1);

        List<AiqVisitAssetCapture> byVisit = visitAssetCaptureRepository.findByTenantIdAndVisitId("tenant-2", "VISIT-002");
        assertThat(byVisit).hasSize(1);

        List<AiqVisitAssetCapture> byOutlet = visitAssetCaptureRepository.findByTenantIdAndOutletCode("tenant-2", "OUT-200");
        assertThat(byOutlet).hasSize(1);
    }
}
