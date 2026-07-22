package com.applicate.services.assetiq.util;

import com.applicate.services.assetiq.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * F10 — converts a manual 1-5 purity_raw_score into a purity_pct.
 *
 * <p>TODO(scoring config): this scale is hardcoded pending aiq_scoring_config,
 * which will make it tenant-configurable. Replace this lookup with a
 * repository-backed one once that table exists — every caller goes through
 * {@link #toPurityPct(Short)}, so that's the only place that will need to change.
 */
public final class PurityScaleConverter {

    private static final Map<Short, BigDecimal> DEFAULT_SCALE = Map.of(
            (short) 1, new BigDecimal("10.00"),
            (short) 2, new BigDecimal("30.00"),
            (short) 3, new BigDecimal("50.00"),
            (short) 4, new BigDecimal("70.00"),
            (short) 5, new BigDecimal("90.00"));

    private PurityScaleConverter() {
    }

    public static BigDecimal toPurityPct(Short rawScore) {
        BigDecimal pct = DEFAULT_SCALE.get(rawScore);
        if (pct == null) {
            throw new BadRequestException("purity_raw_score must be between 1 and 5");
        }
        return pct;
    }
}
