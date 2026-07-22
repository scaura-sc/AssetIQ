package com.applicate.services.assetiq.dto.visit;

import com.applicate.services.assetiq.entity.enums.BrandingStatus;
import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.PresenceStatus;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import com.applicate.services.assetiq.entity.enums.ScanMethod;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * F09/F10. Two notes on fields that stand in for tables that don't exist yet:
 * <ul>
 *   <li>{@code roleCode} — without aiq_user_ref_cache there's no live user-&gt;role
 *       lookup, so the visiting user's role is passed explicitly here.
 *       TODO: replace with a real user lookup once that cache exists.</li>
 *   <li>{@code isPlannedVisit} — without aiq_visit_plan_dates this can't be
 *       matched against a real journey plan, so it's accepted as explicit
 *       input (defaults false). TODO: real PJP matching once that table exists.</li>
 * </ul>
 *
 * <p>Purity input is one of two mutually exclusive paths: supply {@code purityRawScore}
 * (1-5) for the MANUAL path (converted to purity_pct via {@link com.applicate.services.assetiq.util.PurityScaleConverter}),
 * or supply {@code purityPct} directly for the AI_VISION path (optionally with
 * {@code purityAiConfidence}). Supplying both is rejected.
 */
public record VisitCaptureCreateRequest(
        @NotBlank String visitId,
        @NotNull LocalDate visitDate,
        @NotBlank String outletCode,
        String territoryCode,
        @NotBlank String salesmanCode,
        @NotNull Long assetId,
        @NotNull RoleCode roleCode,
        boolean isPlannedVisit,
        @NotNull PresenceStatus presenceStatus,
        ScanMethod scanMethod,
        String scanValue,
        @Min(1) @Max(5) Short purityRawScore,
        BigDecimal purityPct,
        BigDecimal purityAiConfidence,
        ConditionGrade conditionGrade,
        BigDecimal conditionAiConfidence,
        WorkingStatus workingStatus,
        boolean competitorPresent,
        String competitorBrand,
        BigDecimal competitorPct,
        BrandingStatus brandingStatus,
        String photoUrl1,
        String photoUrl2,
        String photoUrl3,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        BigDecimal gpsAccuracyM,
        boolean capturedOffline,
        LocalDateTime syncedAt,
        @NotNull LocalDateTime capturedAt,
        String notes
) {
}
