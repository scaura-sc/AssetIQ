package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.BrandingStatus;
import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.DetectionSource;
import com.applicate.services.assetiq.entity.enums.PresenceStatus;
import com.applicate.services.assetiq.entity.enums.ScanMethod;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.entity.enums.WorkingStatusSource;
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
 * Transaction: a single field-visit's capture of one asset's presence,
 * purity, condition, working status, competitor info, and photos/GPS.
 * {@code asset_id} is a soft reference to {@code aiq_asset.id} — no DB-level FK.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_visit_asset_capture")
public class AiqVisitAssetCapture extends AbstractLogEntity {

    /** External SFA visit reference. */
    @Column(name = "visit_id", nullable = false, length = 50)
    private String visitId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "outlet_code", nullable = false, length = 50)
    private String outletCode;

    @Column(name = "territory_code", length = 30)
    private String territoryCode;

    @Column(name = "salesman_code", nullable = false, length = 50)
    private String salesmanCode;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    /** True when this visit matches a planned aiq_visit_plan_dates row. */
    @Column(name = "is_planned_visit", nullable = false)
    private Boolean isPlannedVisit = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "presence_status", nullable = false, length = 20)
    private PresenceStatus presenceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_method", length = 20)
    private ScanMethod scanMethod;

    @Column(name = "scan_value", length = 100)
    private String scanValue;

    /** Manual 1-5 scale. */
    @Column(name = "purity_raw_score")
    private Short purityRawScore;

    /** Source of truth for AHS. */
    @Column(name = "purity_pct", precision = 5, scale = 2)
    private BigDecimal purityPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "purity_source", nullable = false, length = 20)
    private DetectionSource puritySource = DetectionSource.MANUAL;

    /** NULL unless purity_source = AI_VISION. */
    @Column(name = "purity_ai_confidence", precision = 5, scale = 2)
    private BigDecimal purityAiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", length = 20)
    private ConditionGrade conditionGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_source", nullable = false, length = 20)
    private DetectionSource conditionSource = DetectionSource.MANUAL;

    /** NULL unless condition_source = AI_VISION. */
    @Column(name = "condition_ai_confidence", precision = 5, scale = 2)
    private BigDecimal conditionAiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_status", length = 20)
    private WorkingStatus workingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_status_source", nullable = false, length = 20)
    private WorkingStatusSource workingStatusSource = WorkingStatusSource.MANUAL;

    @Column(name = "competitor_present", nullable = false)
    private Boolean competitorPresent = false;

    @Column(name = "competitor_brand", length = 50)
    private String competitorBrand;

    @Column(name = "competitor_pct", precision = 5, scale = 2)
    private BigDecimal competitorPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "branding_status", length = 20)
    private BrandingStatus brandingStatus;

    @Column(name = "photo_url_1", length = 500)
    private String photoUrl1;

    @Column(name = "photo_url_2", length = 500)
    private String photoUrl2;

    @Column(name = "photo_url_3", length = 500)
    private String photoUrl3;

    @Column(name = "gps_lat", precision = 10, scale = 8)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 11, scale = 8)
    private BigDecimal gpsLng;

    @Column(name = "gps_accuracy_m", precision = 8, scale = 2)
    private BigDecimal gpsAccuracyM;

    @Column(name = "captured_offline", nullable = false)
    private Boolean capturedOffline = false;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "notes", length = 500)
    private String notes;
}
