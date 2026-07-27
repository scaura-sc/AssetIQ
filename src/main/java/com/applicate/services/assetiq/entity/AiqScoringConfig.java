package com.applicate.services.assetiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Master: tenant-wide AHS (Asset Health Score) component weightages — one row
 * per tenant. No {@code is_active} — like {@link AiqRoleConfig}, this is a
 * small curated config, not a soft-deletable operational record. The 4
 * weights are expected (validated in ScoringConfigService) to sum to exactly
 * 100.00.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_scoring_config")
public class AiqScoringConfig extends AbstractAuditableEntity {

    @Column(name = "presence_weight_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal presenceWeightPct;

    @Column(name = "purity_weight_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal purityWeightPct;

    @Column(name = "condition_weight_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal conditionWeightPct;

    @Column(name = "uptime_weight_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal uptimeWeightPct;
}
