package com.applicate.services.assetiq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables {@code @CreatedDate} / {@code @LastModifiedDate} handling so
 * {@link com.applicate.services.assetiq.entity.AbstractAuditableEntity#getCreatedAt()}
 * and {@code getUpdatedAt()} are populated automatically on every entity that
 * extends it, without any service-layer bookkeeping.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
