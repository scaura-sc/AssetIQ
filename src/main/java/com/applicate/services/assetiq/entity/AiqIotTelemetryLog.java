package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.AlertType;
import com.applicate.services.assetiq.entity.enums.MetricType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Transaction: raw high-frequency IoT sensor readings. {@code asset_id} is a
 * soft reference to {@code aiq_asset.id} — no DB-level FK.
 *
 * <p>{@code metric_value} is deliberately a plain string (not typed per
 * metric_type) so one wide table can carry temperature readings, power
 * on/off states, door-open events, GPS coordinates, etc. without a schema
 * change per metric.
 */
@Getter
@Setter
@Entity
@Table(name = "aiq_iot_telemetry_log")
public class AiqIotTelemetryLog extends AbstractLogEntity {

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_number", nullable = false, length = 30)
    private String assetNumber;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 30)
    private MetricType metricType;

    @Column(name = "metric_value", nullable = false, length = 50)
    private String metricValue;

    @Column(name = "metric_unit", length = 20)
    private String metricUnit;

    @Column(name = "alert_triggered", nullable = false)
    private Boolean alertTriggered = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 50)
    private AlertType alertType;

    /** Device-side timestamp. */
    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    /** Server-side receipt timestamp. */
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
