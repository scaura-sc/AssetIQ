package com.applicate.services.assetiq.dto.assetrequest;

import jakarta.validation.constraints.NotBlank;

public record AssetRequestRejectRequest(
        @NotBlank String rejectedByUserCode,
        @NotBlank String rejectionReason
) {
}
