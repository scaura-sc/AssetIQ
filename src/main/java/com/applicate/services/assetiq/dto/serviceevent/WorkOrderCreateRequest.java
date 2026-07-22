package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.TriggeredBy;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** F15. total_cost is never accepted from the client — always recomputed server-side from labour_cost + parts_cost. */
public record WorkOrderCreateRequest(
        @NotNull Long assetId,
        @NotBlank String outletCode,
        @NotNull Priority priority,
        @NotBlank String raisedByUserCode,
        String assignedToUserCode,
        @NotNull WorkOrderType woType,
        @NotNull TriggeredBy triggeredBy,
        LocalDate plannedDate,
        BigDecimal labourCost,
        BigDecimal partsCost,
        String checklistSummary,
        @NotNull LocalDateTime raisedAt
) {
}
