package com.applicate.services.assetiq.dto.assetrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Category+type only — the outlet doesn't know or care which specific
 * model/asset it gets, only what kind. status is always PENDING on
 * creation — there is no field for it here, by design.
 */
public record AssetRequestCreateRequest(
        @NotBlank String outletCode,
        String outletName,
        String territoryCode,
        @NotBlank String salesmanCode,
        @NotBlank String categoryCode,
        @NotBlank String typeCode,
        String reason,
        @NotBlank String requestedByUserCode,
        @NotNull LocalDateTime requestedAt
) {
}
