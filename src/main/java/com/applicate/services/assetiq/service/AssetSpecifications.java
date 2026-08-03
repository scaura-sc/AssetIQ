package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Backs GET /assets/fleet-snapshot's server-side filtering (territory/outlet/status/model/search). */
final class AssetSpecifications {

    private AssetSpecifications() {
    }

    static Specification<AiqAsset> filter(String tenantId, String territoryCode, String locationCode,
                                           AssetStatus assetStatus, String modelCode, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (territoryCode != null) {
                predicates.add(cb.equal(root.get("territoryCode"), territoryCode));
            }
            if (locationCode != null) {
                predicates.add(cb.equal(root.get("locationCode"), locationCode));
            }
            if (assetStatus != null) {
                predicates.add(cb.equal(root.get("assetStatus"), assetStatus));
            }
            if (modelCode != null) {
                predicates.add(cb.equal(root.get("modelCode"), modelCode));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("assetNumber")), pattern),
                        cb.like(cb.lower(root.get("assetName")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
