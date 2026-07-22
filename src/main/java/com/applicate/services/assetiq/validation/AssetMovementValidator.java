package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.exception.ConflictException;
import com.applicate.services.assetiq.repository.AssetMovementLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Enforces the state-machine invariants around deploy/transfer/swap: STOCK+WORKING
 * preconditions, the RETIRED/SCRAPPED terminal-state lock, and the interim
 * "marked for retrieval" rule for transfers out of an outlet (see
 * {@link #validateTransferFromOutletAllowed} javadoc — this is a pragmatic
 * stand-in until a real retrieval-request entity exists).
 */
@Component
public class AssetMovementValidator {

    private static final Set<AssetStatus> TERMINAL_STATUSES = Set.of(AssetStatus.RETIRED, AssetStatus.SCRAPPED);

    private final AssetMovementLogRepository assetMovementLogRepository;

    public AssetMovementValidator(AssetMovementLogRepository assetMovementLogRepository) {
        this.assetMovementLogRepository = assetMovementLogRepository;
    }

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

    /**
     * F06 pragmatic interim rule: since there's no retrieval-request entity yet,
     * a transfer FROM an outlet is only allowed if a prior TRANSFER/SWAP_OUT
     * movement already exists for this asset, moved out of that outlet (to a
     * non-OUTLET location) after the current association's assignment_date —
     * i.e. someone has already logged pulling it out of active deployment.
     * A real "retrieval request" workflow may need its own entity later.
     */
    public void validateTransferFromOutletAllowed(String tenantId, AiqAsset asset, AiqAssetAssociation currentAssociation) {
        if (currentAssociation.getLocationType() != LocationType.OUTLET) {
            return;
        }
        List<AiqAssetMovementLog> history =
                assetMovementLogRepository.findByTenantIdAndAssetIdOrderByMovedAtDesc(tenantId, asset.getId());
        // assignment_date is date-only (no time-of-day), so "dated after" is interpreted as
        // on-or-after that calendar date — a same-day retrieval marking is common in fast field-ops
        // turnaround and shouldn't be rejected just because both events land on the same date.
        boolean alreadyMarkedForRetrieval = history.stream().anyMatch(m ->
                (m.getMovementType() == MovementType.TRANSFER || m.getMovementType() == MovementType.SWAP_OUT)
                        && m.getToLocationType() != LocationType.OUTLET
                        && !m.getMovedAt().toLocalDate().isBefore(currentAssociation.getAssignmentDate()));
        if (!alreadyMarkedForRetrieval) {
            throw new ConflictException(
                    "Asset " + asset.getAssetNumber() + " is not marked for retrieval — a prior TRANSFER/SWAP_OUT"
                            + " moving it out of the outlet is required before it can be transferred (interim rule,"
                            + " pending a real retrieval-request entity)");
        }
    }
}
