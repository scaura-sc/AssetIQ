package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Backs GET /visit-captures/search. Unlike movements, territory_code is a real column here. */
final class VisitCaptureSpecifications {

    private VisitCaptureSpecifications() {
    }

    static Specification<AiqVisitAssetCapture> filter(String tenantId, Long assetId, String territoryCode,
                                                        LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (assetId != null) {
                predicates.add(cb.equal(root.get("assetId"), assetId));
            }
            if (territoryCode != null) {
                predicates.add(cb.equal(root.get("territoryCode"), territoryCode));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capturedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("capturedAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
