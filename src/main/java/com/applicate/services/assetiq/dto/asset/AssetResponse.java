package com.applicate.services.assetiq.dto.asset;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AhsConfidenceLevel;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.WarrantyType;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssetResponse(
        Long id,
        String tenantId,
        String assetNumber,
        String serialNumber,
        String assetName,
        String categoryCode,
        String typeCode,
        String modelCode,
        String vendorCode,
        String brandCode,
        String divisionCode,
        String companyCode,
        BigDecimal capacity,
        String capacityUnit,
        String colour,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        String purchaseOrderRef,
        String invoiceRef,
        LocalDate manufacturingDate,
        LocalDate warrantyStartDate,
        LocalDate warrantyEndDate,
        WarrantyType warrantyType,
        LocalDate amcStartDate,
        LocalDate amcEndDate,
        String amcVendorCode,
        DepreciationMethod depreciationMethod,
        Short usefulLifeYears,
        BigDecimal residualValue,
        AssetStatus assetStatus,
        WorkingStatus workingStatus,
        ConditionGrade conditionGrade,
        LocationType locationType,
        String locationCode,
        String territoryCode,
        String salesmanCode,
        LocalDate installationDate,
        LocalDate lastVisitDate,
        String lastVisitId,
        BigDecimal ahsScore,
        BigDecimal ahsPresenceScore,
        BigDecimal ahsPurityScore,
        BigDecimal ahsConditionScore,
        BigDecimal ahsUptimeScore,
        BigDecimal ahsPlFactor,
        AhsConfidenceLevel ahsConfidenceLevel,
        LocalDateTime ahsCalculatedAt,
        Boolean ahsStaleFlag,
        LocalDateTime ahsStaleSince,
        String primaryPhotoUrl,
        String documentRefs,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        Boolean isActive
) {
    public static AssetResponse from(AiqAsset e) {
        return new AssetResponse(
                e.getId(), e.getTenantId(), e.getAssetNumber(), e.getSerialNumber(), e.getAssetName(),
                e.getCategoryCode(), e.getTypeCode(), e.getModelCode(), e.getVendorCode(), e.getBrandCode(),
                e.getDivisionCode(), e.getCompanyCode(), e.getCapacity(), e.getCapacityUnit(), e.getColour(),
                e.getPurchaseDate(), e.getPurchasePrice(), e.getPurchaseOrderRef(), e.getInvoiceRef(),
                e.getManufacturingDate(), e.getWarrantyStartDate(), e.getWarrantyEndDate(), e.getWarrantyType(),
                e.getAmcStartDate(), e.getAmcEndDate(), e.getAmcVendorCode(), e.getDepreciationMethod(),
                e.getUsefulLifeYears(), e.getResidualValue(), e.getAssetStatus(), e.getWorkingStatus(),
                e.getConditionGrade(), e.getLocationType(), e.getLocationCode(), e.getTerritoryCode(),
                e.getSalesmanCode(), e.getInstallationDate(), e.getLastVisitDate(), e.getLastVisitId(),
                e.getAhsScore(), e.getAhsPresenceScore(), e.getAhsPurityScore(), e.getAhsConditionScore(),
                e.getAhsUptimeScore(), e.getAhsPlFactor(), e.getAhsConfidenceLevel(), e.getAhsCalculatedAt(),
                e.getAhsStaleFlag(), e.getAhsStaleSince(), e.getPrimaryPhotoUrl(), e.getDocumentRefs(),
                e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedBy(), e.getUpdatedAt(), e.getIsActive());
    }
}
