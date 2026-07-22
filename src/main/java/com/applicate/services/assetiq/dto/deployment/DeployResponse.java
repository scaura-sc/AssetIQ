package com.applicate.services.assetiq.dto.deployment;

import com.applicate.services.assetiq.dto.association.AssociationResponse;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.movement.MovementLogResponse;

public record DeployResponse(AssetResponse asset, AssociationResponse association, MovementLogResponse movementLog) {
}
