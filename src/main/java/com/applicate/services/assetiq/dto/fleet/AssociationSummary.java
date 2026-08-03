package com.applicate.services.assetiq.dto.fleet;

import com.applicate.services.assetiq.entity.AiqAssetAssociation;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Current-association fields the fleet-snapshot consumers actually read — no history. */
public record AssociationSummary(
        String locationName,
        LocalDate assignmentDate,
        String custodianName,
        Boolean hasContract,
        BigDecimal purityClausePct,
        Boolean exclusivityFlag
) {
    public static AssociationSummary from(AiqAssetAssociation e) {
        return new AssociationSummary(
                e.getLocationName(), e.getAssignmentDate(), e.getCustodianName(),
                e.getHasContract(), e.getPurityClausePct(), e.getExclusivityFlag());
    }
}
