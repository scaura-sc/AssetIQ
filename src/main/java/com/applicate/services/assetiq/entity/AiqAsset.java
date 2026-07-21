package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.AhsConfidenceLevel;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.WarrantyType;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
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
 * Association (core entity): the physical asset record — identity,
 * commercial/warranty terms, current status/location, and rolling AHS
 * (Asset Health Score) fields.
 *
 * <p>{@code category_code}/{@code type_code}/{@code model_code} are soft
 * references into {@code aiq_asset_catalog} (by level); {@code vendor_code}
 * and {@code amc_vendor_code} soft-reference {@code aiq_vendor};
 * {@code location_code} is generic and its meaning depends on
 * {@code location_type}. None of these are DB-level foreign keys.
 *
 * <p>{@code created_by}/{@code updated_by} are plain actor-identity columns
 * set by the service layer from the caller's context — unlike
 * {@code created_at}/{@code updated_at}, they are not auto-populated by JPA
 * auditing (no {@code AuditorAware} is wired up), so callers must set them
 * explicitly before every save.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_asset")
public class AiqAsset extends AbstractSoftDeletableEntity {

    // --- Identity ---
    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    @Column(name = "serial_number", nullable = false, length = 50)
    private String serialNumber;

    @Column(name = "asset_name", nullable = false, length = 100)
    private String assetName;

    @Column(name = "category_code", nullable = false, length = 30)
    private String categoryCode;

    @Column(name = "type_code", nullable = false, length = 30)
    private String typeCode;

    @Column(name = "model_code", nullable = false, length = 30)
    private String modelCode;

    @Column(name = "vendor_code", length = 30)
    private String vendorCode;

    @Column(name = "brand_code", length = 30)
    private String brandCode;

    @Column(name = "division_code", length = 30)
    private String divisionCode;

    @Column(name = "company_code", length = 20)
    private String companyCode;

    @Column(name = "capacity", precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit;

    @Column(name = "colour", length = 30)
    private String colour;

    // --- Commercial / warranty / AMC ---
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_order_ref", length = 50)
    private String purchaseOrderRef;

    @Column(name = "invoice_ref", length = 50)
    private String invoiceRef;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "warranty_start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_type", length = 20)
    private WarrantyType warrantyType;

    @Column(name = "amc_start_date")
    private LocalDate amcStartDate;

    @Column(name = "amc_end_date")
    private LocalDate amcEndDate;

    @Column(name = "amc_vendor_code", length = 30)
    private String amcVendorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", length = 5)
    private DepreciationMethod depreciationMethod;

    @Column(name = "useful_life_years")
    private Short usefulLifeYears;

    @Column(name = "residual_value", precision = 14, scale = 2)
    private BigDecimal residualValue;

    // --- Status / location ---
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_status", nullable = false, length = 30)
    private AssetStatus assetStatus = AssetStatus.STOCK;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_status", length = 20)
    private WorkingStatus workingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", length = 20)
    private ConditionGrade conditionGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", length = 20)
    private LocationType locationType;

    @Column(name = "location_code", length = 50)
    private String locationCode;

    @Column(name = "territory_code", length = 30)
    private String territoryCode;

    @Column(name = "salesman_code", length = 30)
    private String salesmanCode;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "last_visit_date")
    private LocalDate lastVisitDate;

    @Column(name = "last_visit_id", length = 50)
    private String lastVisitId;

    // --- AHS (Asset Health Score) ---
    @Column(name = "ahs_score", precision = 5, scale = 2)
    private BigDecimal ahsScore;

    @Column(name = "ahs_presence_score", precision = 5, scale = 2)
    private BigDecimal ahsPresenceScore;

    @Column(name = "ahs_purity_score", precision = 5, scale = 2)
    private BigDecimal ahsPurityScore;

    @Column(name = "ahs_condition_score", precision = 5, scale = 2)
    private BigDecimal ahsConditionScore;

    @Column(name = "ahs_uptime_score", precision = 5, scale = 2)
    private BigDecimal ahsUptimeScore;

    @Column(name = "ahs_pl_factor", precision = 5, scale = 2)
    private BigDecimal ahsPlFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "ahs_confidence_level", length = 10)
    private AhsConfidenceLevel ahsConfidenceLevel;

    @Column(name = "ahs_calculated_at")
    private LocalDateTime ahsCalculatedAt;

    @Column(name = "ahs_stale_flag", nullable = false)
    private Boolean ahsStaleFlag = false;

    @Column(name = "ahs_stale_since")
    private LocalDateTime ahsStaleSince;

    // --- Media / docs ---
    @Column(name = "primary_photo_url", length = 500)
    private String primaryPhotoUrl;

    /** Comma-separated URLs. */
    @Column(name = "document_refs", length = 1000)
    private String documentRefs;

    // --- Actor audit (separate from created_at/updated_at; not auto-populated) ---
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
}
