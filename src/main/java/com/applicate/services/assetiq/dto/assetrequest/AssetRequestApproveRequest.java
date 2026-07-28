package com.applicate.services.assetiq.dto.assetrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** assetId must be one of the ids returned by GET /{id}/available-stock — enforced by reusing
 * AssetDeploymentService.deploy's own preconditions, not re-checked here. */
public record AssetRequestApproveRequest(
        @NotNull Long assetId,
        @NotBlank String approvedByUserCode,
        String reason
) {
}
