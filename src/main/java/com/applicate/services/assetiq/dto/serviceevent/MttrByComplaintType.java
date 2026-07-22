package com.applicate.services.assetiq.dto.serviceevent;

import com.applicate.services.assetiq.entity.enums.ComplaintType;

/** F17 — "per category" is interpreted as per complaint_type, since aiq_service_event_log has no category column. */
public record MttrByComplaintType(ComplaintType complaintType, double avgResolutionHours, long sampleSize) {
}
