package com.applicate.services.assetiq.idgen;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a BIGINT {@code @Id} field as application-generated via
 * {@link SnowflakeIdGenerator}, instead of relying on the database
 * (AUTO_INCREMENT / SERIAL / IDENTITY), per the multi-engine design rule.
 *
 * Usage:
 * <pre>
 *     {@literal @}Id
 *     {@literal @}SnowflakeGenerator
 *     {@literal @}Column(name = "id", nullable = false, updatable = false)
 *     private Long id;
 * </pre>
 */
@IdGeneratorType(SnowflakeIdGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface SnowflakeGenerator {
}
