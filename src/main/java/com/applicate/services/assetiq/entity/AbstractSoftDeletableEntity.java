package com.applicate.services.assetiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Adds the {@code is_active} soft-delete flag on top of {@link AbstractAuditableEntity}.
 * Rows in transaction/association tables (and most masters) must never be
 * hard-deleted — flip this instead.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractSoftDeletableEntity extends AbstractAuditableEntity {

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
