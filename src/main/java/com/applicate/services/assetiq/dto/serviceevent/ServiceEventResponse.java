package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.TriggeredBy;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One flat response for both COMPLAINT and WORK_ORDER rows, mirroring the single-entity design. */
public record ServiceEventResponse(
        Long id,
        String tenantId,
        String eventNumber,
        EventType eventType,
        Long assetId,
        String assetNumber,
        String outletCode,
        String visitId,
        Priority priority,
        EventStatus status,
        String description,
        String raisedByUserCode,
        String assignedToUserCode,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt,
        String resolutionNotes,
        String photoUrl1,
        String photoUrl2,
        String photoAfterUrl,
        String signatureUrl,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        ComplaintType complaintType,
        Boolean isUnderWarranty,
        Boolean isRepeated,
        String parentEventNumber,
        Short customerRating,
        WorkOrderType woType,
        TriggeredBy triggeredBy,
        LocalDate plannedDate,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        BigDecimal labourCost,
        BigDecimal partsCost,
        BigDecimal totalCost,
        String checklistSummary,
        LocalDateTime raisedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ServiceEventResponse from(AiqServiceEventLog e) {
        return new ServiceEventResponse(
                e.getId(), e.getTenantId(), e.getEventNumber(), e.getEventType(), e.getAssetId(), e.getAssetNumber(),
                e.getOutletCode(), e.getVisitId(), e.getPriority(), e.getStatus(), e.getDescription(),
                e.getRaisedByUserCode(), e.getAssignedToUserCode(), e.getResolvedAt(), e.getClosedAt(),
                e.getResolutionNotes(), e.getPhotoUrl1(), e.getPhotoUrl2(), e.getPhotoAfterUrl(), e.getSignatureUrl(),
                e.getGpsLat(), e.getGpsLng(), e.getComplaintType(), e.getIsUnderWarranty(), e.getIsRepeated(),
                e.getParentEventNumber(), e.getCustomerRating(), e.getWoType(), e.getTriggeredBy(), e.getPlannedDate(),
                e.getStartedAt(), e.getCompletedAt(), e.getLabourCost(), e.getPartsCost(), e.getTotalCost(),
                e.getChecklistSummary(), e.getRaisedAt(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
