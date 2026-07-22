package com.applicate.services.assetiq.dto.vendor;

import com.applicate.services.assetiq.entity.AiqVendor;
import com.applicate.services.assetiq.entity.enums.VendorType;

import java.time.LocalDateTime;

public record VendorResponse(
        Long id,
        String tenantId,
        String vendorCode,
        String vendorName,
        VendorType vendorType,
        String gstNumber,
        String contactEmail,
        String contactPhone,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static VendorResponse from(AiqVendor e) {
        return new VendorResponse(
                e.getId(), e.getTenantId(), e.getVendorCode(), e.getVendorName(), e.getVendorType(),
                e.getGstNumber(), e.getContactEmail(), e.getContactPhone(), e.getIsActive(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
