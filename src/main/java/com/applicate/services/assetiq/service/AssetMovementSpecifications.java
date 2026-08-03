package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs GET /assets/movements/search. "territory" isn't a column on
 * aiq_asset_movement_log — the caller resolves territory to a set of asset ids
 * via aiq_asset.territory_code first (see AssetDeploymentService.searchMovements),
 * same approach as ServiceEventSpecifications.
 */
final class AssetMovementSpecifications {

    private AssetMovementSpecifications() {
    }

    static Specification<AiqAssetMovementLog> filter(String tenantId, Long assetId, LocalDateTime from, LocalDateTime to,
                                                       List<Long> territoryAssetIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (assetId != null) {
                predicates.add(cb.equal(root.get("assetId"), assetId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("movedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("movedAt"), to));
            }
            if (territoryAssetIds != null) {
                predicates.add(territoryAssetIds.isEmpty() ? cb.disjunction() : root.get("assetId").in(territoryAssetIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
