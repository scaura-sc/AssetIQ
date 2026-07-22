package com.applicate.services.assetiq.dto.movement;

import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementLogResponse(
        Long id,
        String tenantId,
        Long assetId,
        String assetNumber,
        MovementType movementType,
        LocationType fromLocationType,
        String fromLocationCode,
        LocationType toLocationType,
        String toLocationCode,
        String movedByUserCode,
        String approvedByUserCode,
        String approvalRef,
        String reason,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        LocalDateTime movedAt
) {
    public static MovementLogResponse from(AiqAssetMovementLog e) {
        return new MovementLogResponse(
                e.getId(), e.getTenantId(), e.getAssetId(), e.getAssetNumber(), e.getMovementType(),
                e.getFromLocationType(), e.getFromLocationCode(), e.getToLocationType(), e.getToLocationCode(),
                e.getMovedByUserCode(), e.getApprovedByUserCode(), e.getApprovalRef(), e.getReason(),
                e.getGpsLat(), e.getGpsLng(), e.getMovedAt());
    }
}
