package com.applicate.services.assetiq.dto.asset;

import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.entity.enums.WarrantyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * asset_number is server-generated (see BusinessCodeGenerator), never accepted
 * from the client. asset_status is always STOCK on creation — there is no
 * field for it here at all, by design.
 */
public record AssetCreateRequest(
        @NotBlank String serialNumber,
        @NotBlank String assetName,
        @NotBlank String categoryCode,
        @NotBlank String typeCode,
        @NotBlank String modelCode,
        String vendorCode,
        String brandCode,
        String divisionCode,
        String companyCode,
        BigDecimal capacity,
        String capacityUnit,
        String colour,
        @NotNull LocalDate purchaseDate,
        @NotNull BigDecimal purchasePrice,
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
        @NotBlank String createdBy
) {
}
