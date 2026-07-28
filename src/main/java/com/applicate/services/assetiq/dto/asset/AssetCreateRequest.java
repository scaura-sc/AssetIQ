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
 *
 * <p>{@code warehouseCode}/{@code territoryCode} are optional initial-placement
 * fields: neither {@code deploy} (outlet-only) nor {@code transfer} (requires
 * a pre-existing association to move from) can give a freshly-registered
 * asset its first warehouse association, so registration itself carries this
 * — if {@code warehouseCode} is given, the new asset is created already
 * sitting in that warehouse instead of with no location at all.
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
        String warehouseCode,
        String territoryCode,
        @NotBlank String createdBy
) {
}
