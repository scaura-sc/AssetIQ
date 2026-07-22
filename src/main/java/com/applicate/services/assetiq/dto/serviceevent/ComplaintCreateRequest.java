package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * F14. is_under_warranty and is_repeated are both computed server-side — there
 * are no fields for them here at all, by design (see ServiceEventService).
 */
public record ComplaintCreateRequest(
        @NotNull Long assetId,
        @NotBlank String outletCode,
        String visitId,
        @NotNull Priority priority,
        String description,
        @NotBlank String raisedByUserCode,
        @NotNull ComplaintType complaintType,
        String photoUrl1,
        String photoUrl2,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        @NotNull LocalDateTime raisedAt
) {
}
