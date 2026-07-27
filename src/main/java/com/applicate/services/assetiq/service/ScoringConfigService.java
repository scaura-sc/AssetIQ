package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.scoringconfig.ScoringConfigRequest;
import com.applicate.services.assetiq.dto.scoringconfig.ScoringConfigResponse;
import com.applicate.services.assetiq.entity.AiqScoringConfig;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.repository.ScoringConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Tenant-wide AHS weightage config — one row per tenant, read far more often
 * than written. {@link #getEffectiveWeights(String)} is the one method
 * {@link AhsCalculationService} depends on; it never throws for a tenant
 * without a configured row — it falls back to an even 25/25/25/25 split.
 */
@Service
@Transactional
public class ScoringConfigService {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_WEIGHT = new BigDecimal("25.00");

    private final ScoringConfigRepository scoringConfigRepository;

    public ScoringConfigService(ScoringConfigRepository scoringConfigRepository) {
        this.scoringConfigRepository = scoringConfigRepository;
    }

    public ScoringConfigResponse get() {
        String tenantId = TenantContext.getTenantId();
        return scoringConfigRepository.findByTenantId(tenantId)
                .map(ScoringConfigResponse::from)
                .orElseGet(() -> new ScoringConfigResponse(
                        null, tenantId, DEFAULT_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT, DEFAULT_WEIGHT, null, null));
    }

    public ScoringConfigResponse upsert(ScoringConfigRequest request) {
        String tenantId = TenantContext.getTenantId();
        validate(request);

        AiqScoringConfig config = scoringConfigRepository.findByTenantId(tenantId).orElseGet(AiqScoringConfig::new);
        config.setTenantId(tenantId);
        config.setPresenceWeightPct(request.presenceWeightPct());
        config.setPurityWeightPct(request.purityWeightPct());
        config.setConditionWeightPct(request.conditionWeightPct());
        config.setUptimeWeightPct(request.uptimeWeightPct());

        return ScoringConfigResponse.from(scoringConfigRepository.save(config));
    }

    /** Used by AhsCalculationService — returns the persisted config, or an unsaved 25/25/25/25 default. */
    AiqScoringConfig getEffectiveWeights(String tenantId) {
        return scoringConfigRepository.findByTenantId(tenantId).orElseGet(() -> {
            AiqScoringConfig defaults = new AiqScoringConfig();
            defaults.setTenantId(tenantId);
            defaults.setPresenceWeightPct(DEFAULT_WEIGHT);
            defaults.setPurityWeightPct(DEFAULT_WEIGHT);
            defaults.setConditionWeightPct(DEFAULT_WEIGHT);
            defaults.setUptimeWeightPct(DEFAULT_WEIGHT);
            return defaults;
        });
    }

    private void validate(ScoringConfigRequest request) {
        BigDecimal[] weights = {
                request.presenceWeightPct(), request.purityWeightPct(),
                request.conditionWeightPct(), request.uptimeWeightPct()
        };
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal weight : weights) {
            if (weight.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Weights must not be negative");
            }
            sum = sum.add(weight);
        }
        if (sum.compareTo(HUNDRED) != 0) {
            throw new BadRequestException("presenceWeightPct + purityWeightPct + conditionWeightPct + uptimeWeightPct must sum to 100, got " + sum);
        }
    }
}
