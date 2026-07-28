package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.visit.VisitCaptureCreateRequest;
import com.applicate.services.assetiq.dto.visit.VisitCaptureResponse;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.DetectionSource;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.ConflictException;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.repository.VisitAssetCaptureRepository;
import com.applicate.services.assetiq.util.PurityScaleConverter;
import com.applicate.services.assetiq.validation.ReferenceValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** F09 — Visit Asset Capture, F10 — Purity Scoring. */
@Service
@Transactional
public class VisitAssetCaptureService {

    private final VisitAssetCaptureRepository visitAssetCaptureRepository;
    private final AssetRepository assetRepository;
    private final ReferenceValidationService referenceValidationService;
    private final AhsCalculationService ahsCalculationService;

    public VisitAssetCaptureService(VisitAssetCaptureRepository visitAssetCaptureRepository,
                                     AssetRepository assetRepository,
                                     ReferenceValidationService referenceValidationService,
                                     AhsCalculationService ahsCalculationService) {
        this.visitAssetCaptureRepository = visitAssetCaptureRepository;
        this.assetRepository = assetRepository;
        this.referenceValidationService = referenceValidationService;
        this.ahsCalculationService = ahsCalculationService;
    }

    public VisitCaptureResponse create(VisitCaptureCreateRequest request) {
        String tenantId = TenantContext.getTenantId();

        AiqAsset asset = referenceValidationService.requireAsset(tenantId, request.assetId());
        AiqRoleConfig role = referenceValidationService.requireRole(tenantId, request.roleCode());

        if (visitAssetCaptureRepository.findByTenantIdAndAssetIdAndVisitId(tenantId, request.assetId(), request.visitId()).isPresent()) {
            throw new ConflictException(
                    "A capture already exists for asset " + request.assetId() + " on visit '" + request.visitId() + "'");
        }

        if (request.purityRawScore() != null && request.purityPct() != null) {
            throw new BadRequestException("Provide either purity_raw_score (MANUAL) or purity_pct (AI_VISION), not both");
        }

        AiqVisitAssetCapture capture = new AiqVisitAssetCapture();
        capture.setTenantId(tenantId);
        capture.setVisitId(request.visitId());
        capture.setVisitDate(request.visitDate());
        capture.setOutletCode(request.outletCode());
        capture.setTerritoryCode(request.territoryCode());
        capture.setSalesmanCode(request.salesmanCode());
        capture.setAssetId(request.assetId());
        capture.setAssetNumber(asset.getAssetNumber());
        // TODO(PJP matching): once aiq_visit_plan_dates exists, derive this from a real journey
        // plan match instead of trusting the caller's flag.
        capture.setIsPlannedVisit(request.isPlannedVisit());
        capture.setPresenceStatus(request.presenceStatus());
        capture.setScanMethod(request.scanMethod());
        capture.setScanValue(request.scanValue());

        applyPurity(capture, request);

        capture.setConditionGrade(request.conditionGrade());
        capture.setConditionSource(request.conditionAiConfidence() != null ? DetectionSource.AI_VISION : DetectionSource.MANUAL);
        capture.setConditionAiConfidence(request.conditionAiConfidence());
        capture.setWorkingStatus(request.workingStatus());
        capture.setCompetitorPresent(request.competitorPresent());
        capture.setCompetitorBrand(request.competitorBrand());
        capture.setCompetitorPct(request.competitorPct());
        capture.setBrandingStatus(request.brandingStatus());
        capture.setPhotoUrl1(request.photoUrl1());
        capture.setPhotoUrl2(request.photoUrl2());
        capture.setPhotoUrl3(request.photoUrl3());
        capture.setGpsLat(request.gpsLat());
        capture.setGpsLng(request.gpsLng());
        capture.setGpsAccuracyM(request.gpsAccuracyM());
        capture.setCapturedOffline(request.capturedOffline());
        capture.setSyncedAt(request.syncedAt());
        capture.setCapturedAt(request.capturedAt());
        capture.setNotes(request.notes());

        capture = visitAssetCaptureRepository.save(capture);

        // Only an asset_capture_eligible role's visit counts toward the asset's official last-visit
        // tracking — pulled from aiq_role_config. TODO(user lookup): role_code is explicit request
        // input for now; replace with a real user->role lookup once aiq_user_ref_cache exists.
        if (Boolean.TRUE.equals(role.getAssetCaptureEligible())) {
            asset.setLastVisitDate(request.visitDate());
            asset.setLastVisitId(request.visitId());
        }
        // The asset's own working_status only ever reflected asset-creation-time default
        // (WORKING) until now — nothing synced it from the field's actual reported state.
        // A capture is the ground truth for it, same as it already is for condition/AHS.
        if (request.workingStatus() != null) {
            asset.setWorkingStatus(request.workingStatus());
        }
        // AHS recalculates on every capture, regardless of role eligibility — that gate is
        // specifically about official PJP last-visit tracking, a separate concern.
        ahsCalculationService.recalculate(asset, capture);
        assetRepository.save(asset);

        return VisitCaptureResponse.from(capture);
    }

    private void applyPurity(AiqVisitAssetCapture capture, VisitCaptureCreateRequest request) {
        if (request.purityRawScore() != null) {
            capture.setPurityRawScore(request.purityRawScore());
            capture.setPurityPct(PurityScaleConverter.toPurityPct(request.purityRawScore()));
            capture.setPuritySource(DetectionSource.MANUAL);
            capture.setPurityAiConfidence(null);
        } else if (request.purityPct() != null) {
            capture.setPurityPct(request.purityPct());
            capture.setPuritySource(DetectionSource.AI_VISION);
            capture.setPurityAiConfidence(request.purityAiConfidence());
        }
        // Neither provided: purity_pct/purity_raw_score stay null, purity_source keeps its
        // MANUAL default — this capture simply didn't record a purity reading.
    }

    public VisitCaptureResponse get(Long id) {
        return VisitCaptureResponse.from(requireOwned(id));
    }

    public List<VisitCaptureResponse> listByAsset(Long assetId) {
        return visitAssetCaptureRepository.findByTenantIdAndAssetIdOrderByCapturedAtDesc(TenantContext.getTenantId(), assetId)
                .stream().map(VisitCaptureResponse::from).toList();
    }

    private AiqVisitAssetCapture requireOwned(Long id) {
        return visitAssetCaptureRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No visit capture with id " + id));
    }
}
