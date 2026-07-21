package com.applicate.services.assetiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base for transaction log tables (movement log, visit capture, IoT telemetry,
 * AI vision results) — {@code created_at} only, no {@code updated_at}, no
 * {@code is_active}. These rows are written once and read many times; even
 * where a later async step fills in a result column (e.g. AI vision
 * processing_status), the table tracks that via its own domain timestamp
 * column rather than a generic updated_at.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractLogEntity extends AbstractTenantEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
