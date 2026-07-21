package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqIotTelemetryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IotTelemetryLogRepository extends JpaRepository<AiqIotTelemetryLog, Long> {

    List<AiqIotTelemetryLog> findByTenantIdAndAssetIdOrderByCapturedAtDesc(String tenantId, Long assetId);

    List<AiqIotTelemetryLog> findByTenantIdAndDeviceId(String tenantId, String deviceId);

    List<AiqIotTelemetryLog> findByTenantIdAndAlertTriggeredTrue(String tenantId);
}
