package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.exception.ConflictException;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Enforces the state-machine invariants around deploy/transfer/swap: STOCK+WORKING
 * preconditions and the RETIRED/SCRAPPED terminal-state lock.
 */
@Component
public class AssetMovementValidator {

    private static final Set<AssetStatus> TERMINAL_STATUSES = Set.of(AssetStatus.RETIRED, AssetStatus.SCRAPPED);

    /** Must be the first check on every movement-affecting write (deploy/transfer/swap) — RETIRED/SCRAPPED is irreversible. */
    public void validateNotTerminal(AiqAsset asset) {
        if (TERMINAL_STATUSES.contains(asset.getAssetStatus())) {
            throw new ConflictException(
                    "Asset " + asset.getAssetNumber() + " is " + asset.getAssetStatus() + " — no further movement is allowed");
        }
    }

    /** F05 deploy preconditions, also reused for F07's incoming swap asset. */
    public void validateDeployable(AiqAsset asset) {
        validateNotTerminal(asset);
        if (asset.getAssetStatus() != AssetStatus.STOCK) {
            throw new ConflictException(
                    "Asset " + asset.getAssetNumber() + " must be STOCK to deploy (is " + asset.getAssetStatus() + ")");
        }
        if (asset.getWorkingStatus() != WorkingStatus.WORKING) {
            throw new ConflictException(
                    "Asset " + asset.getAssetNumber() + " must be WORKING to deploy (is " + asset.getWorkingStatus() + ")");
        }
    }
}
