package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitAssetCaptureRepository
        extends JpaRepository<AiqVisitAssetCapture, Long>, JpaSpecificationExecutor<AiqVisitAssetCapture> {

    Optional<AiqVisitAssetCapture> findByTenantIdAndId(String tenantId, Long id);

    List<AiqVisitAssetCapture> findByTenantIdAndAssetIdOrderByCapturedAtDesc(String tenantId, Long assetId);

    List<AiqVisitAssetCapture> findByTenantIdAndVisitId(String tenantId, String visitId);

    Optional<AiqVisitAssetCapture> findByTenantIdAndAssetIdAndVisitId(String tenantId, Long assetId, String visitId);

    List<AiqVisitAssetCapture> findByTenantIdAndOutletCode(String tenantId, String outletCode);

    /**
     * One row per asset id: the capture whose captured_at is the max for that asset. Ties (two
     * captures at the exact same captured_at) return more than one row for that asset — callers
     * reduce those themselves (see FleetSnapshotService), since there's no portable single-row-per-
     * group tiebreak across MySQL/Postgres without a window function.
     */
    @Query("""
            SELECT c FROM AiqVisitAssetCapture c
            WHERE c.tenantId = :tenantId
              AND c.assetId IN :assetIds
              AND c.capturedAt = (
                  SELECT MAX(c2.capturedAt) FROM AiqVisitAssetCapture c2
                  WHERE c2.tenantId = c.tenantId AND c2.assetId = c.assetId
              )
            """)
    List<AiqVisitAssetCapture> findLatestByTenantIdAndAssetIdIn(@Param("tenantId") String tenantId, @Param("assetIds") List<Long> assetIds);
}
