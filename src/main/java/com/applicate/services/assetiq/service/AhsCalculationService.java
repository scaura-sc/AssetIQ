package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqScoringConfig;
import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.AhsConfidenceLevel;
import com.applicate.services.assetiq.util.AhsScoreMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Recomputes an asset's AHS (Asset Health Score) from a single visit capture,
 * combining 4 components — presence, purity, condition, uptime (sourced from
 * the capture's working_status) — via the tenant's configured weights.
 *
 * <p>A component this capture didn't report scores 0, not the asset's prior
 * value and not a re-normalized average of what's present — an incomplete
 * capture is meant to visibly pull the score down, by design (product
 * decision: missing data must reduce AHS, not be silently ignored). presence
 * always contributes a real score since presence_status is a required field
 * on every capture.
 */
@Service
public class AhsCalculationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final ScoringConfigService scoringConfigService;

    public AhsCalculationService(ScoringConfigService scoringConfigService) {
        this.scoringConfigService = scoringConfigService;
    }

    /** Mutates {@code asset}'s ahs_* fields in place. Caller is responsible for persisting it. */
    public void recalculate(AiqAsset asset, AiqVisitAssetCapture capture) {
        BigDecimal presenceScore = AhsScoreMapper.mapPresence(capture.getPresenceStatus());
        BigDecimal purityScore = capture.getPurityPct() != null ? capture.getPurityPct() : BigDecimal.ZERO;
        BigDecimal conditionScore = capture.getConditionGrade() != null
                ? AhsScoreMapper.mapCondition(capture.getConditionGrade()) : BigDecimal.ZERO;
        BigDecimal uptimeScore = capture.getWorkingStatus() != null
                ? AhsScoreMapper.mapWorking(capture.getWorkingStatus()) : BigDecimal.ZERO;

        AiqScoringConfig weights = scoringConfigService.getEffectiveWeights(asset.getTenantId());

        BigDecimal weightedSum = presenceScore.multiply(weights.getPresenceWeightPct())
                .add(purityScore.multiply(weights.getPurityWeightPct()))
                .add(conditionScore.multiply(weights.getConditionWeightPct()))
                .add(uptimeScore.multiply(weights.getUptimeWeightPct()));
        BigDecimal composite = weightedSum.divide(HUNDRED, 2, RoundingMode.HALF_UP);

        int capturedCount = 1 // presence always captured
                + (capture.getPurityPct() != null ? 1 : 0)
                + (capture.getConditionGrade() != null ? 1 : 0)
                + (capture.getWorkingStatus() != null ? 1 : 0);
        AhsConfidenceLevel confidence = switch (capturedCount) {
            case 4 -> AhsConfidenceLevel.HIGH;
            case 3 -> AhsConfidenceLevel.MEDIUM;
            default -> AhsConfidenceLevel.LOW;
        };

        asset.setAhsPresenceScore(presenceScore);
        asset.setAhsPurityScore(purityScore);
        asset.setAhsConditionScore(conditionScore);
        asset.setAhsUptimeScore(uptimeScore);
        asset.setAhsScore(composite);
        asset.setAhsConfidenceLevel(confidence);
        asset.setAhsCalculatedAt(LocalDateTime.now());
        asset.setAhsStaleFlag(false);
        asset.setAhsStaleSince(null);
    }
}
