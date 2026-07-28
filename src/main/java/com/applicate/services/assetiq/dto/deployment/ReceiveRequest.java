package com.applicate.services.assetiq.dto.deployment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Receives a freshly-registered (or otherwise unlocated) STOCK asset into a WAREHOUSE — the
 * asset's first-ever association, same as deploy() but for warehouse stock instead of an
 * outlet. Deliberately minimal next to DeployRequest: warehouse stock has no outlet contract,
 * deposit, or exclusivity concept.
 */
public record ReceiveRequest(
        @NotBlank String warehouseCode,
        String warehouseName,
        String territoryCode,
        @NotNull LocalDate assignmentDate,
        @NotBlank String movedByUserCode,
        String reason
) {
}
