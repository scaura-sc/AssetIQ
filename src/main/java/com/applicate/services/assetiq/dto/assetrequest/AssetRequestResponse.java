package com.applicate.services.assetiq.dto.assetrequest;

import com.applicate.services.assetiq.entity.AiqAssetRequest;
import com.applicate.services.assetiq.entity.enums.AssetRequestStatus;

import java.time.LocalDateTime;

public record AssetRequestResponse(
        Long id,
        String tenantId,
        String requestNumber,
        String outletCode,
        String outletName,
        String territoryCode,
        String salesmanCode,
        String categoryCode,
        String typeCode,
        String reason,
        AssetRequestStatus status,
        String requestedByUserCode,
        LocalDateTime requestedAt,
        Long approvedAssetId,
        String approvedByUserCode,
        LocalDateTime approvedAt,
        String rejectionReason,
        String rejectedByUserCode,
        LocalDateTime rejectedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AssetRequestResponse from(AiqAssetRequest e) {
        return new AssetRequestResponse(
                e.getId(), e.getTenantId(), e.getRequestNumber(), e.getOutletCode(), e.getOutletName(),
                e.getTerritoryCode(), e.getSalesmanCode(), e.getCategoryCode(), e.getTypeCode(), e.getReason(),
                e.getStatus(), e.getRequestedByUserCode(), e.getRequestedAt(), e.getApprovedAssetId(),
                e.getApprovedByUserCode(), e.getApprovedAt(), e.getRejectionReason(), e.getRejectedByUserCode(),
                e.getRejectedAt(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
