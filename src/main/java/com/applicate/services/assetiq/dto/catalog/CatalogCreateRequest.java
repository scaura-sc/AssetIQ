package com.applicate.services.assetiq.dto.catalog;

import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CatalogCreateRequest(
        @NotNull CatalogLevel level,
        @NotBlank String code,
        @NotBlank String name,
        /** Required for TYPE (a CATEGORY code) and MODEL (a TYPE code); must be null for CATEGORY. */
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
        String capacityUnit
) {
}
