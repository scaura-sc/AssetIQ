package com.applicate.services.assetiq.dto.vendor;

import com.applicate.services.assetiq.entity.enums.VendorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VendorCreateRequest(
        @NotBlank String vendorCode,
        @NotBlank String vendorName,
        @NotNull VendorType vendorType,
        String gstNumber,
        String contactEmail,
        String contactPhone
) {
}
