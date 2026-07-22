package com.applicate.services.assetiq.dto.serviceevent;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * F15 closure — photo_after_url and signature_url are mandatory, enforced by
 * the @NotBlank here (structural enforcement, not a redundant service check).
 * labour_cost/parts_cost are optional final figures — if given they override
 * whatever was set at creation; total_cost is always recomputed server-side.
 */
public record CloseWorkOrderRequest(
        @NotBlank String photoAfterUrl,
        @NotBlank String signatureUrl,
        String resolutionNotes,
        BigDecimal labourCost,
        BigDecimal partsCost
) {
}
