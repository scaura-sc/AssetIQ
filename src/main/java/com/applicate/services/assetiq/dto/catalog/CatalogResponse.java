package com.applicate.services.assetiq.dto.catalog;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CatalogResponse(
        Long id,
        String tenantId,
        CatalogLevel level,
        String code,
        String name,
        String parentCode,
        String description,
        String manufacturerName,
        String manufacturerCountry,
        String manufacturerContactEmail,
        String manufacturerContactPhone,
        Short defaultWarrantyMonths,
        Short defaultUsefulLifeYears,
        DepreciationMethod defaultDepreciationMethod,
        Short defaultPmFrequencyDays,
        BigDecimal defaultPurityClausePct,
        BigDecimal capacity,
        String capacityUnit,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CatalogResponse from(AiqAssetCatalog e) {
        return new CatalogResponse(
                e.getId(), e.getTenantId(), e.getLevel(), e.getCode(), e.getName(), e.getParentCode(),
                e.getDescription(), e.getManufacturerName(), e.getManufacturerCountry(),
                e.getManufacturerContactEmail(), e.getManufacturerContactPhone(),
                e.getDefaultWarrantyMonths(), e.getDefaultUsefulLifeYears(), e.getDefaultDepreciationMethod(),
                e.getDefaultPmFrequencyDays(), e.getDefaultPurityClausePct(), e.getCapacity(), e.getCapacityUnit(),
                e.getIsActive(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
