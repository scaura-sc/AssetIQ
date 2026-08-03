package com.applicate.services.assetiq.dto.fleet;

import com.applicate.services.assetiq.dto.asset.AssetResponse;

public record FleetSnapshotItem(
        AssetResponse asset,
        AssociationSummary currentAssociation,
        CaptureSummary latestCapture
) {
}
