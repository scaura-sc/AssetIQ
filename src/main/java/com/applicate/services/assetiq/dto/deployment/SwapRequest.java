package com.applicate.services.assetiq.dto.deployment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Replaces oldAssetId (currently deployed) with newAssetId (currently STOCK)
 * at the same outlet, in one transaction. Both resulting movement log rows
 * share a single operation reference: we reuse approval_ref for this (rather
 * than adding a new swap_reference column) — if the caller doesn't supply
 * one, the service mints one via BusinessCodeGenerator so the pair is still
 * traceable.
 */
public record SwapRequest(
        @NotNull Long oldAssetId,
        @NotNull Long newAssetId,
        String swapReference,
        @NotBlank String movedByUserCode,
        String reason,
        java.math.BigDecimal gpsLat,
        java.math.BigDecimal gpsLng
) {
}
