package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitAssetCaptureRepository extends JpaRepository<AiqVisitAssetCapture, Long> {

    Optional<AiqVisitAssetCapture> findByTenantIdAndId(String tenantId, Long id);

    List<AiqVisitAssetCapture> findByTenantIdAndAssetIdOrderByCapturedAtDesc(String tenantId, Long assetId);

    List<AiqVisitAssetCapture> findByTenantIdAndVisitId(String tenantId, String visitId);

    Optional<AiqVisitAssetCapture> findByTenantIdAndAssetIdAndVisitId(String tenantId, Long assetId, String visitId);

    List<AiqVisitAssetCapture> findByTenantIdAndOutletCode(String tenantId, String outletCode);
}
