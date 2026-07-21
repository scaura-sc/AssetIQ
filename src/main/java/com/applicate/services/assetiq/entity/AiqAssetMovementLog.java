package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction: immutable append-only history of every asset movement.
 * {@code asset_id} is a soft reference to {@code aiq_asset.id} — no DB-level FK.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_asset_movement_log")
public class AiqAssetMovementLog extends AbstractLogEntity {

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    /** NULL for an asset's first assignment (no prior location). */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_location_type", length = 20)
    private LocationType fromLocationType;

    @Column(name = "from_location_code", length = 50)
    private String fromLocationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_location_type", length = 20)
    private LocationType toLocationType;

    @Column(name = "to_location_code", length = 50)
    private String toLocationCode;

    @Column(name = "moved_by_user_code", nullable = false, length = 50)
    private String movedByUserCode;

    @Column(name = "approved_by_user_code", length = 50)
    private String approvedByUserCode;

    @Column(name = "approval_ref", length = 50)
    private String approvalRef;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "gps_lat", precision = 10, scale = 8)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 11, scale = 8)
    private BigDecimal gpsLng;

    @Column(name = "photo_before_url", length = 500)
    private String photoBeforeUrl;

    @Column(name = "photo_after_url", length = 500)
    private String photoAfterUrl;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "moved_at", nullable = false)
    private LocalDateTime movedAt;
}
