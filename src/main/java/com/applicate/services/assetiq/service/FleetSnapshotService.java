package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.fleet.AssociationSummary;
import com.applicate.services.assetiq.dto.fleet.CaptureSummary;
import com.applicate.services.assetiq.dto.fleet.FleetSnapshotItem;
import com.applicate.services.assetiq.dto.fleet.FleetSnapshotResponse;
import com.applicate.services.assetiq.dto.fleet.PageMeta;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.repository.AssetAssociationRepository;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.repository.VisitAssetCaptureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fleet-wide read model backing the dashboard pages that previously listed
 * all assets and fanned out 3 per-asset calls each (association, movements,
 * visit-captures) — see docs/backend-requirements-fleet-snapshot.md in the
 * AssetIQ-Dashboard frontend repo (commit a86f460). Only the current
 * association and latest capture are joined in per row; no consumer reads
 * association or movement history from this endpoint, so it's deliberately
 * not embedded here — that stays on the existing per-asset endpoints.
 */
@Service
@Transactional(readOnly = true)
public class FleetSnapshotService {

    private final AssetRepository assetRepository;
    private final AssetAssociationRepository assetAssociationRepository;
    private final VisitAssetCaptureRepository visitAssetCaptureRepository;

    public FleetSnapshotService(AssetRepository assetRepository,
                                 AssetAssociationRepository assetAssociationRepository,
                                 VisitAssetCaptureRepository visitAssetCaptureRepository) {
        this.assetRepository = assetRepository;
        this.assetAssociationRepository = assetAssociationRepository;
        this.visitAssetCaptureRepository = visitAssetCaptureRepository;
    }

    public FleetSnapshotResponse snapshot(String territoryCode, String locationCode, AssetStatus assetStatus,
                                           String modelCode, String search, int page, int size) {
        String tenantId = TenantContext.getTenantId();

        var spec = AssetSpecifications.filter(tenantId, territoryCode, locationCode, assetStatus, modelCode, search);
        Page<AiqAsset> assetPage = assetRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Long> assetIds = assetPage.getContent().stream().map(AiqAsset::getId).toList();
        Map<Long, AssociationSummary> associationsByAssetId = currentAssociationsByAssetId(tenantId, assetIds);
        Map<Long, CaptureSummary> capturesByAssetId = latestCapturesByAssetId(tenantId, assetIds);

        List<FleetSnapshotItem> items = assetPage.getContent().stream()
                .map(asset -> new FleetSnapshotItem(
                        AssetResponse.from(asset),
                        associationsByAssetId.get(asset.getId()),
                        capturesByAssetId.get(asset.getId())))
                .toList();

        PageMeta pageMeta = new PageMeta(assetPage.getNumber(), assetPage.getSize(), assetPage.getTotalElements(), assetPage.getTotalPages());
        return new FleetSnapshotResponse(items, pageMeta);
    }

    private Map<Long, AssociationSummary> currentAssociationsByAssetId(String tenantId, List<Long> assetIds) {
        if (assetIds.isEmpty()) {
            return Map.of();
        }
        return assetAssociationRepository.findByTenantIdAndAssetIdInAndIsActiveTrue(tenantId, assetIds).stream()
                .collect(Collectors.toMap(AiqAssetAssociation::getAssetId, AssociationSummary::from, (a, b) -> a));
    }

    private Map<Long, CaptureSummary> latestCapturesByAssetId(String tenantId, List<Long> assetIds) {
        if (assetIds.isEmpty()) {
            return Map.of();
        }
        return visitAssetCaptureRepository.findLatestByTenantIdAndAssetIdIn(tenantId, assetIds).stream()
                .collect(Collectors.toMap(
                        AiqVisitAssetCapture::getAssetId,
                        CaptureSummary::from,
                        // Tie-break on captured_at (possible when the correlated-subquery match
                        // returns >1 row for the same asset) by keeping either — they're equal by
                        // captured_at, and no consumer distinguishes between them.
                        (a, b) -> a));
    }
}
