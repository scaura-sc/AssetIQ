package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.exception.ConflictException;
import com.applicate.services.assetiq.repository.AssetAssociationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Enforces "only one active aiq_asset_association per asset" — a schema
 * invariant that can't be a DB constraint (no partial/filtered unique index
 * is portable across MySQL/Postgres, see the changeset comment on
 * aiq_asset_association).
 */
@Component
public class ActiveAssociationValidator {

    private final AssetAssociationRepository assetAssociationRepository;

    public ActiveAssociationValidator(AssetAssociationRepository assetAssociationRepository) {
        this.assetAssociationRepository = assetAssociationRepository;
    }

    public Optional<AiqAssetAssociation> findActive(String tenantId, Long assetId) {
        return assetAssociationRepository.findByTenantIdAndAssetIdAndIsActiveTrue(tenantId, assetId);
    }

    /** Call immediately before inserting a new active association row (after any prior one has been deactivated). */
    public void validateNoActiveAssociation(String tenantId, Long assetId) {
        if (findActive(tenantId, assetId).isPresent()) {
            throw new ConflictException("Asset " + assetId + " already has an active association; deactivate it first");
        }
    }
}
