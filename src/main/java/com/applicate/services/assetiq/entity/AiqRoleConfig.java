package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.RoleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Master: per-role eligibility config for field asset-capture visits.
 * No {@code is_active} — this table is a small, curated set of role
 * definitions maintained alongside the {@link RoleCode} enum, not a
 * soft-deletable operational record.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_role_config")
public class AiqRoleConfig extends AbstractAuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 30)
    private RoleCode roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "asset_capture_eligible", nullable = false)
    private Boolean assetCaptureEligible = false;

    @Column(name = "description", length = 255)
    private String description;
}
