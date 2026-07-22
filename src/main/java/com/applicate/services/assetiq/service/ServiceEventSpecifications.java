package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * F17 dashboard filters. "territory" isn't a column on aiq_service_event_log
 * (it only has outlet_code) — the caller resolves territory to a set of
 * asset ids via aiq_asset.territory_code first (see ServiceEventService) and
 * passes that in as assetIds, since there's no DB-level join to do it here.
 */
final class ServiceEventSpecifications {

    private ServiceEventSpecifications() {
    }

    static Specification<AiqServiceEventLog> filter(String tenantId, EventType eventType, EventStatus status,
                                                      Priority priority, String assignedToUserCode,
                                                      LocalDateTime raisedAfter, LocalDateTime raisedBefore,
                                                      List<Long> assetIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assignedToUserCode != null) {
                predicates.add(cb.equal(root.get("assignedToUserCode"), assignedToUserCode));
            }
            if (raisedAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("raisedAt"), raisedAfter));
            }
            if (raisedBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("raisedAt"), raisedBefore));
            }
            if (assetIds != null) {
                predicates.add(assetIds.isEmpty() ? cb.disjunction() : root.get("assetId").in(assetIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
