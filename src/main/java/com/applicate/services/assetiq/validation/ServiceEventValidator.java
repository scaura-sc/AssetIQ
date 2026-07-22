package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.ConflictException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Enforces the COMPLAINT/WORK_ORDER field-shape split on aiq_service_event_log
 * (single flat entity, no JPA inheritance — see the entity javadoc) and the
 * status lifecycle.
 */
@Component
public class ServiceEventValidator {

    /**
     * Linear progression order. REOPENED is not in this map — it's reachable
     * only from RESOLVED/CLOSED, and once reached behaves like ASSIGNED for
     * outgoing transitions (see {@link #validateStatusTransition}).
     */
    private static final Map<EventStatus, Integer> ORDER = new EnumMap<>(EventStatus.class);

    static {
        ORDER.put(EventStatus.OPEN, 0);
        ORDER.put(EventStatus.ASSIGNED, 1);
        ORDER.put(EventStatus.IN_PROGRESS, 2);
        ORDER.put(EventStatus.RESOLVED, 3);
        ORDER.put(EventStatus.CLOSED, 4);
    }

    /** Validates the COMPLAINT-only / WORK_ORDER-only column split for a new or updated event. */
    public void validateFieldShape(AiqServiceEventLog event) {
        if (event.getEventType() == EventType.COMPLAINT) {
            if (event.getComplaintType() == null) {
                throw new BadRequestException("complaint_type is required when event_type = COMPLAINT");
            }
            if (event.getWoType() != null || event.getTriggeredBy() != null || event.getPlannedDate() != null
                    || event.getStartedAt() != null || event.getCompletedAt() != null
                    || event.getLabourCost() != null || event.getPartsCost() != null
                    || event.getTotalCost() != null || event.getChecklistSummary() != null) {
                throw new BadRequestException("WORK_ORDER-only fields must be null when event_type = COMPLAINT");
            }
        } else {
            if (event.getWoType() == null) {
                throw new BadRequestException("wo_type is required when event_type = WORK_ORDER");
            }
            if (event.getComplaintType() != null || event.getParentEventNumber() != null
                    || event.getCustomerRating() != null) {
                throw new BadRequestException("COMPLAINT-only fields must be null when event_type = WORK_ORDER");
            }
        }
    }

    /** Validates a status transition against the OPEN&lt;ASSIGNED&lt;IN_PROGRESS&lt;RESOLVED&lt;CLOSED lifecycle, with REOPENED as a re-entry point. */
    public void validateStatusTransition(EventStatus current, EventStatus next) {
        if (current == next) {
            throw new ConflictException("Event is already in status " + current);
        }

        if (next == EventStatus.REOPENED) {
            if (current != EventStatus.RESOLVED && current != EventStatus.CLOSED) {
                throw new ConflictException("Can only reopen from RESOLVED or CLOSED (was " + current + ")");
            }
            return;
        }

        int currentOrder = current == EventStatus.REOPENED ? ORDER.get(EventStatus.ASSIGNED) : ORDER.get(current);
        Integer nextOrder = ORDER.get(next);
        if (nextOrder == null || nextOrder <= currentOrder) {
            throw new ConflictException("Cannot transition from " + current + " to " + next);
        }
    }
}
