package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.idgen.SnowflakeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Root base for every AssetIQ entity: {@code id} (application-generated
 * Snowflake BIGINT PK — never DB auto-increment/serial/identity, so
 * generation behaves identically on MySQL and Postgres) and {@code tenant_id}
 * (multi-tenancy discriminator; subclasses must make this the first column of
 * every composite index they define).
 *
 * <p>No column here (or on any subclass) is a foreign key at the DB level —
 * all cross-entity references are soft references validated in the service
 * layer, per the no-FK design rule.
 *
 * <p>Deliberately has no timestamp columns — see {@link AbstractAuditableEntity}
 * (created_at + updated_at, for rows that get updated) and
 * {@link AbstractLogEntity} (created_at only, for append-only transaction logs).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractTenantEntity {

    @Id
    @SnowflakeGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractTenantEntity that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
