package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.TriggeredBy;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction: complaints and maintenance work orders, merged into one table
 * discriminated by {@code event_type}. Per design instruction, this is a
 * single flat JPA entity — no {@code @Inheritance}/discriminator mapping —
 * with all COMPLAINT-only and WORK_ORDER-only columns nullable; enforcing
 * that the right group is populated for a given event_type is a
 * service-layer concern, not an entity or DB constraint.
 *
 * <p>{@code asset_id} is a soft reference to {@code aiq_asset.id} — no DB-level FK.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_service_event_log")
public class AiqServiceEventLog extends AbstractAuditableEntity {

    // --- Common ---
    @Column(name = "event_number", nullable = false, length = 20)
    private String eventNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    @Column(name = "outlet_code", nullable = false, length = 50)
    private String outletCode;

    @Column(name = "visit_id", length = 50)
    private String visitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status = EventStatus.OPEN;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "raised_by_user_code", nullable = false, length = 50)
    private String raisedByUserCode;

    @Column(name = "assigned_to_user_code", length = 50)
    private String assignedToUserCode;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    @Column(name = "photo_url_1", length = 500)
    private String photoUrl1;

    @Column(name = "photo_url_2", length = 500)
    private String photoUrl2;

    @Column(name = "photo_after_url", length = 500)
    private String photoAfterUrl;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "gps_lat", precision = 10, scale = 8)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 11, scale = 8)
    private BigDecimal gpsLng;

    // --- COMPLAINT-only (NULL when event_type = WORK_ORDER) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", length = 50)
    private ComplaintType complaintType;

    @Column(name = "is_under_warranty", nullable = false)
    private Boolean isUnderWarranty = false;

    /** Same complaint_type, same asset, within 30 days — computed by the service layer. */
    @Column(name = "is_repeated", nullable = false)
    private Boolean isRepeated = false;

    @Column(name = "parent_event_number", length = 20)
    private String parentEventNumber;

    /** Post-closure 1-5. */
    @Column(name = "customer_rating")
    private Short customerRating;

    // --- WORK_ORDER-only (NULL when event_type = COMPLAINT) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "wo_type", length = 20)
    private WorkOrderType woType;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", length = 20)
    private TriggeredBy triggeredBy;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "labour_cost", precision = 10, scale = 2)
    private BigDecimal labourCost;

    @Column(name = "parts_cost", precision = 10, scale = 2)
    private BigDecimal partsCost;

    /** Feeds the P&L cost rollup. */
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "checklist_summary", length = 1000)
    private String checklistSummary;

    // --- common (cont.) ---
    @Column(name = "raised_at", nullable = false)
    private LocalDateTime raisedAt;
}
