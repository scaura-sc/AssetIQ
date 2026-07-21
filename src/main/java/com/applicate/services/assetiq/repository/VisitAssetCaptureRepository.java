package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitAssetCaptureRepository extends JpaRepository<AiqVisitAssetCapture, Long> {

    List<AiqVisitAssetCapture> findByTenantIdAndAssetIdOrderByCapturedAtDesc(String tenantId, Long assetId);

    List<AiqVisitAssetCapture> findByTenantIdAndVisitId(String tenantId, String visitId);

    List<AiqVisitAssetCapture> findByTenantIdAndOutletCode(String tenantId, String outletCode);
}
