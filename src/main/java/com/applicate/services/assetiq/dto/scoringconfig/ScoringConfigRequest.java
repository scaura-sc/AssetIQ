package com.applicate.services.assetiq.dto.scoringconfig;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** The 4 weights must be >= 0 and sum to exactly 100.00 — validated in ScoringConfigService. */
public record ScoringConfigRequest(
        @NotNull BigDecimal presenceWeightPct,
        @NotNull BigDecimal purityWeightPct,
        @NotNull BigDecimal conditionWeightPct,
        @NotNull BigDecimal uptimeWeightPct
) {
}
