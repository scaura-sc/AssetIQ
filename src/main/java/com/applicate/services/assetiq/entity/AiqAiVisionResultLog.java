package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.ProcessingStatus;
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
 * Transaction: AI photo-analysis output for a field-visit capture.
 * {@code visit_capture_id} soft-references {@code aiq_visit_asset_capture.id}
 * and {@code asset_id} soft-references {@code aiq_asset.id} — neither is a
 * DB-level FK.
 *
 * <p>{@code detected_condition}/{@code detected_brand} are left as plain
 * strings rather than enums: they're raw model output whose label vocabulary
 * can shift between model versions (see {@code model_version}), unlike the
 * fixed, human-curated {@code ConditionGrade} set used elsewhere.
 * {@code raw_result} stores the full model response as a JSON string
 * (VARCHAR, not a native JSON/JSONB column) per the no-vendor-types rule.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_ai_vision_result_log")
public class AiqAiVisionResultLog extends AbstractLogEntity {

    @Column(name = "visit_capture_id", nullable = false)
    private Long visitCaptureId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    /** Enables reproducibility of a given result against the model that produced it. */
    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "detected_purity_pct", precision = 5, scale = 2)
    private BigDecimal detectedPurityPct;

    @Column(name = "detected_condition", length = 20)
    private String detectedCondition;

    @Column(name = "detected_brand", length = 50)
    private String detectedBrand;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /** Full model output as a JSON string, for audit — not a native JSON/JSONB column. */
    @Column(name = "raw_result", length = 2000)
    private String rawResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
