package com.applicate.services.assetiq.dto.catalog;

import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/** level and parent_code are structural and immutable after creation — not editable here. */
public record CatalogUpdateRequest(
        @NotBlank String name,
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
