package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqAssetRequest;
import com.applicate.services.assetiq.entity.enums.AssetRequestStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class AssetRequestSpecifications {

    private AssetRequestSpecifications() {
    }

    static Specification<AiqAssetRequest> filter(String tenantId, AssetRequestStatus status,
                                                   String outletCode, String territoryCode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (outletCode != null) {
                predicates.add(cb.equal(root.get("outletCode"), outletCode));
            }
            if (territoryCode != null) {
                predicates.add(cb.equal(root.get("territoryCode"), territoryCode));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
