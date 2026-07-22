package com.applicate.services.assetiq.dto.association;

import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.DepositStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssociationResponse(
        Long id,
        String tenantId,
        Long assetId,
        String assetNumber,
        LocationType locationType,
        String locationCode,
        String locationName,
        String territoryCode,
        String salesmanCode,
        String custodianName,
        String custodianPhone,
        LocalDate assignmentDate,
        LocalDate expectedReturnDate,
        AssignmentType assignmentType,
        String assignmentRef,
        Boolean hasContract,
        String contractRef,
        BigDecimal depositAmount,
        DepositStatus depositStatus,
        Boolean exclusivityFlag,
        BigDecimal purityClausePct,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        String contractDocumentUrl,
        Boolean isActive,
        String createdBy,
        LocalDateTime createdAt
) {
    public static AssociationResponse from(AiqAssetAssociation e) {
        return new AssociationResponse(
                e.getId(), e.getTenantId(), e.getAssetId(), e.getAssetNumber(), e.getLocationType(),
                e.getLocationCode(), e.getLocationName(), e.getTerritoryCode(), e.getSalesmanCode(),
                e.getCustodianName(), e.getCustodianPhone(), e.getAssignmentDate(), e.getExpectedReturnDate(),
                e.getAssignmentType(), e.getAssignmentRef(), e.getHasContract(), e.getContractRef(),
                e.getDepositAmount(), e.getDepositStatus(), e.getExclusivityFlag(), e.getPurityClausePct(),
                e.getContractStartDate(), e.getContractEndDate(), e.getContractDocumentUrl(), e.getIsActive(),
                e.getCreatedBy(), e.getCreatedAt());
    }
}
