package com.applicate.services.assetiq.dto.scoringconfig;

import com.applicate.services.assetiq.entity.AiqScoringConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScoringConfigResponse(
        Long id,
        String tenantId,
        BigDecimal presenceWeightPct,
        BigDecimal purityWeightPct,
        BigDecimal conditionWeightPct,
        BigDecimal uptimeWeightPct,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ScoringConfigResponse from(AiqScoringConfig e) {
        return new ScoringConfigResponse(
                e.getId(), e.getTenantId(), e.getPresenceWeightPct(), e.getPurityWeightPct(),
                e.getConditionWeightPct(), e.getUptimeWeightPct(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
