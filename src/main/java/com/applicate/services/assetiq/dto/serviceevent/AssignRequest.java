package com.applicate.services.assetiq.dto.serviceevent;

import jakarta.validation.constraints.NotBlank;

/** Assigns/reassigns a technician on an existing event, independent of the status-transition endpoint and of event_type. */
public record AssignRequest(@NotBlank String assignedToUserCode) {
}
