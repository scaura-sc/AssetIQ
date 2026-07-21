package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.DepositStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Association: current asset-to-location link, merged with contract terms
 * (discriminated by {@code has_contract} — {@code contract_*}/{@code deposit_*}
 * columns are only populated when it's true; enforcing that per-column
 * requirement is a service-layer concern, not a DB constraint).
 *
 * <p>{@code asset_id} is a soft reference to {@code aiq_asset.id} — no DB-level
 * FK. Only one row per asset should have {@code is_active = true} at a time;
 * since a partial/filtered unique index isn't portable across MySQL and
 * Postgres, that invariant is enforced in the service layer, not the schema.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_asset_association")
public class AiqAssetAssociation extends AbstractSoftDeletableEntity {

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    /** Generic soft reference; e.g. an outlet_code when location_type = OUTLET. */
    @Column(name = "location_code", nullable = false, length = 50)
    private String locationCode;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "territory_code", length = 30)
    private String territoryCode;

    @Column(name = "salesman_code", length = 30)
    private String salesmanCode;

    @Column(name = "custodian_name", length = 100)
    private String custodianName;

    @Column(name = "custodian_phone", length = 20)
    private String custodianPhone;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    /** TEMPORARY assignments only. */
    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private AssignmentType assignmentType = AssignmentType.PERMANENT;

    @Column(name = "assignment_ref", length = 50)
    private String assignmentRef;

    /** Discriminator: when true, the contract and deposit columns below apply. */
    @Column(name = "has_contract", nullable = false)
    private Boolean hasContract = false;

    @Column(name = "contract_ref", length = 50)
    private String contractRef;

    @Column(name = "deposit_amount", precision = 14, scale = 2)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status", length = 20)
    private DepositStatus depositStatus;

    @Column(name = "exclusivity_flag", nullable = false)
    private Boolean exclusivityFlag = false;

    /** Overrides the catalog default_purity_clause_pct. */
    @Column(name = "purity_clause_pct", precision = 5, scale = 2)
    private BigDecimal purityClausePct;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "contract_document_url", length = 500)
    private String contractDocumentUrl;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
}
