package com.applicate.services.assetiq.dto.visit;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.BrandingStatus;
import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.DetectionSource;
import com.applicate.services.assetiq.entity.enums.PresenceStatus;
import com.applicate.services.assetiq.entity.enums.ScanMethod;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.entity.enums.WorkingStatusSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitCaptureResponse(
        Long id,
        String tenantId,
        String visitId,
        LocalDate visitDate,
        String outletCode,
        String territoryCode,
        String salesmanCode,
        Long assetId,
        String assetNumber,
        Boolean isPlannedVisit,
        PresenceStatus presenceStatus,
        ScanMethod scanMethod,
        String scanValue,
        Short purityRawScore,
        BigDecimal purityPct,
        DetectionSource puritySource,
        BigDecimal purityAiConfidence,
        ConditionGrade conditionGrade,
        DetectionSource conditionSource,
        BigDecimal conditionAiConfidence,
        WorkingStatus workingStatus,
        WorkingStatusSource workingStatusSource,
        Boolean competitorPresent,
        String competitorBrand,
        BigDecimal competitorPct,
        BrandingStatus brandingStatus,
        String photoUrl1,
        String photoUrl2,
        String photoUrl3,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        BigDecimal gpsAccuracyM,
        Boolean capturedOffline,
        LocalDateTime syncedAt,
        LocalDateTime capturedAt,
        LocalDateTime createdAt,
        String notes
) {
    public static VisitCaptureResponse from(AiqVisitAssetCapture e) {
        return new VisitCaptureResponse(
                e.getId(), e.getTenantId(), e.getVisitId(), e.getVisitDate(), e.getOutletCode(), e.getTerritoryCode(),
                e.getSalesmanCode(), e.getAssetId(), e.getAssetNumber(), e.getIsPlannedVisit(), e.getPresenceStatus(),
                e.getScanMethod(), e.getScanValue(), e.getPurityRawScore(), e.getPurityPct(), e.getPuritySource(),
                e.getPurityAiConfidence(), e.getConditionGrade(), e.getConditionSource(), e.getConditionAiConfidence(),
                e.getWorkingStatus(), e.getWorkingStatusSource(), e.getCompetitorPresent(), e.getCompetitorBrand(),
                e.getCompetitorPct(), e.getBrandingStatus(), e.getPhotoUrl1(), e.getPhotoUrl2(), e.getPhotoUrl3(),
                e.getGpsLat(), e.getGpsLng(), e.getGpsAccuracyM(), e.getCapturedOffline(), e.getSyncedAt(),
                e.getCapturedAt(), e.getCreatedAt(), e.getNotes());
    }
}
