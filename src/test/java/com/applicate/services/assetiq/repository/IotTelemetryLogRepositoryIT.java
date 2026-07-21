package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqIotTelemetryLog;
import com.applicate.services.assetiq.entity.enums.AlertType;
import com.applicate.services.assetiq.entity.enums.MetricType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IotTelemetryLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private IotTelemetryLogRepository iotTelemetryLogRepository;

    @Test
    void persistsReadingWithStringMetricValue() {
        AiqIotTelemetryLog reading = new AiqIotTelemetryLog();
        reading.setTenantId("tenant-1");
        reading.setAssetId(1001L);
        reading.setAssetNumber("AST-0001");
        reading.setDeviceId("DEV-1");
        reading.setMetricType(MetricType.TEMPERATURE);
        reading.setMetricValue("4.2");
        reading.setMetricUnit("celsius");
        reading.setCapturedAt(LocalDateTime.of(2025, 5, 1, 10, 0));
        reading.setReceivedAt(LocalDateTime.of(2025, 5, 1, 10, 0, 5));

        AiqIotTelemetryLog saved = iotTelemetryLogRepository.saveAndFlush(reading);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAlertTriggered()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void persistsAlertAndFindsByDeviceAndAlertFlag() {
        AiqIotTelemetryLog alertReading = new AiqIotTelemetryLog();
        alertReading.setTenantId("tenant-2");
        alertReading.setAssetId(2002L);
        alertReading.setAssetNumber("AST-0002");
        alertReading.setDeviceId("DEV-2");
        alertReading.setMetricType(MetricType.TEMPERATURE);
        alertReading.setMetricValue("12.0");
        alertReading.setMetricUnit("celsius");
        alertReading.setAlertTriggered(true);
        alertReading.setAlertType(AlertType.TEMP_HIGH);
        alertReading.setCapturedAt(LocalDateTime.of(2025, 5, 2, 10, 0));
        alertReading.setReceivedAt(LocalDateTime.of(2025, 5, 2, 10, 0, 5));
        iotTelemetryLogRepository.saveAndFlush(alertReading);

        List<AiqIotTelemetryLog> byDevice = iotTelemetryLogRepository.findByTenantIdAndDeviceId("tenant-2", "DEV-2");
        assertThat(byDevice).hasSize(1);

        List<AiqIotTelemetryLog> alerts = iotTelemetryLogRepository.findByTenantIdAndAlertTriggeredTrue("tenant-2");
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(AlertType.TEMP_HIGH);
    }
}
