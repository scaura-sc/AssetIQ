package com.applicate.services.assetiq.dto.fleet;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.ScanMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Latest-capture fields the fleet-snapshot consumers actually read — no capture feed. */
public record CaptureSummary(
        LocalDateTime capturedAt,
        ScanMethod scanMethod,
        BigDecimal purityPct,
        Boolean competitorPresent,
        String competitorBrand
) {
    public static CaptureSummary from(AiqVisitAssetCapture e) {
        return new CaptureSummary(
                e.getCapturedAt(), e.getScanMethod(), e.getPurityPct(), e.getCompetitorPresent(), e.getCompetitorBrand());
    }
}
