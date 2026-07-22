package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** F16. Always wo_type=PREVENTIVE, triggered_by=SCHEDULE — no fields for either, by design. */
public record PreventiveMaintenanceRequest(
        @NotNull Long assetId,
        @NotBlank String outletCode,
        @NotNull Priority priority,
        @NotBlank String raisedByUserCode,
        String assignedToUserCode,
        @NotNull LocalDate plannedDate,
        @NotNull LocalDateTime raisedAt
) {
}
