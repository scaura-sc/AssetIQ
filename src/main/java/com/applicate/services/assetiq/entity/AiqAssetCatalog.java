package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Master: self-referencing CATEGORY -> TYPE -> MODEL hierarchy.
 *
 * <p>{@code parent_code} is a soft reference to another row's {@code code}
 * within the same tenant (never a DB-level FK, and not a JPA
 * {@code @ManyToOne} — hierarchy traversal is done via repository lookups in
 * the service layer). MODEL-level-only columns are nullable and left
 * unenforced at the entity level; validating that they're populated only for
 * {@code level == MODEL} is a service-layer concern.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_asset_catalog")
public class AiqAssetCatalog extends AbstractSoftDeletableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    private CatalogLevel level;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Soft reference to another {@code aiq_asset_catalog.code} in the same tenant; NULL for CATEGORY. */
    @Column(name = "parent_code", length = 30)
    private String parentCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "manufacturer_name", length = 100)
    private String manufacturerName;

    @Column(name = "manufacturer_country", length = 50)
    private String manufacturerCountry;

    @Column(name = "manufacturer_contact_email", length = 100)
    private String manufacturerContactEmail;

    @Column(name = "manufacturer_contact_phone", length = 20)
    private String manufacturerContactPhone;

    @Column(name = "default_warranty_months")
    private Short defaultWarrantyMonths;

    @Column(name = "default_useful_life_years")
    private Short defaultUsefulLifeYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_depreciation_method", length = 5)
    private DepreciationMethod defaultDepreciationMethod;

    @Column(name = "default_pm_frequency_days")
    private Short defaultPmFrequencyDays;

    @Column(name = "default_purity_clause_pct", precision = 5, scale = 2)
    private BigDecimal defaultPurityClausePct;

    @Column(name = "capacity", precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit;
}
