package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.AssetRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An outlet's request for an asset by category+type (no model — the outlet
 * doesn't know or care which model, only what kind of asset it needs). Portal
 * approval picks a specific in-stock asset and deploys it via
 * AssetDeploymentService — approved_asset_id is a soft reference to
 * aiq_asset.id, set only once status transitions to APPROVED.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_asset_request")
public class AiqAssetRequest extends AbstractAuditableEntity {

    @Column(name = "request_number", nullable = false, length = 50)
    private String requestNumber;

    @Column(name = "outlet_code", nullable = false, length = 50)
    private String outletCode;

    @Column(name = "outlet_name", length = 100)
    private String outletName;

    @Column(name = "territory_code", length = 30)
    private String territoryCode;

    @Column(name = "salesman_code", nullable = false, length = 50)
    private String salesmanCode;

    @Column(name = "category_code", nullable = false, length = 30)
    private String categoryCode;

    @Column(name = "type_code", nullable = false, length = 30)
    private String typeCode;

    @Column(name = "reason", length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssetRequestStatus status = AssetRequestStatus.PENDING;

    @Column(name = "requested_by_user_code", nullable = false, length = 50)
    private String requestedByUserCode;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_asset_id")
    private Long approvedAssetId;

    @Column(name = "approved_by_user_code", length = 50)
    private String approvedByUserCode;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "rejected_by_user_code", length = 50)
    private String rejectedByUserCode;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
}
