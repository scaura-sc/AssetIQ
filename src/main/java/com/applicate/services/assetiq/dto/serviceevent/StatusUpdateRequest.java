package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.enums.EventStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull EventStatus status) {
}
