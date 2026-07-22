package com.applicate.services.assetiq.dto.deployment;

import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.DepositStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Deploys an asset to an OUTLET (location_type is always OUTLET for this
 * flow — see the "Asset Deployment to Outlet" feature name; use transfer for
 * any other destination type). Contract fields are all optional: if
 * purity_clause_pct is omitted, it stays null — falling back to the model's
 * default_purity_clause_pct is an AHS-engine concern, out of scope here.
 */
public record DeployRequest(
        @NotBlank String outletCode,
        String outletName,
        String territoryCode,
        String salesmanCode,
        String custodianName,
        String custodianPhone,
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
        String reason,
        /** Freeform for now — DEFERRED: validate against the outlet's registered GPS once aiq_outlet_ref_cache exists. */
        BigDecimal gpsLat,
        BigDecimal gpsLng
) {
}
