package com.applicate.services.assetiq.dto.deployment;

import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.DepositStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Moves an asset to any destination location type. {@code targetAssetStatus}
 * is explicit rather than inferred from the destination (there's no
 * LocationType value for "repair location") — the calling workflow (a
 * relocation screen vs. a repair-intake screen vs. a retirement screen)
 * already knows its own intent. Allowed values: DEPLOYED, UNDER_REPAIR,
 * STOCK, RETIRED, SCRAPPED — the latter two are irreversible.
 */
public record TransferRequest(
        @NotNull LocationType toLocationType,
        @NotBlank String toLocationCode,
        String toLocationName,
        String territoryCode,
        String salesmanCode,
        String custodianName,
        String custodianPhone,
        @NotNull AssetStatus targetAssetStatus,
        @NotNull LocalDate assignmentDate,
        LocalDate expectedReturnDate,
        AssignmentType assignmentType,
        String assignmentRef,
        boolean hasContract,
        String contractRef,
        BigDecimal depositAmount,
        DepositStatus depositStatus,
        boolean exclusivityFlag,
        BigDecimal purityClausePct,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        String contractDocumentUrl,
        @NotBlank String movedByUserCode,
        String approvedByUserCode,
        String approvalRef,
        @NotBlank String reason,
        BigDecimal gpsLat,
        BigDecimal gpsLng
) {
}
