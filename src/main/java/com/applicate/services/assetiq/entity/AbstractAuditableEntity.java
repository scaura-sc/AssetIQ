package com.applicate.services.assetiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Adds {@code created_at}/{@code updated_at} on top of {@link AbstractTenantEntity},
 * auto-populated by {@link AuditingEntityListener} (see
 * {@link com.applicate.services.assetiq.config.JpaAuditingConfig}). For rows
 * that are updated after creation but don't need a soft-delete flag (e.g.
 * aiq_role_config, aiq_service_event_log). See also {@link AbstractSoftDeletableEntity}
 * (+ is_active) and {@link AbstractLogEntity} (created_at only, no updates at all).
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditableEntity extends AbstractTenantEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
