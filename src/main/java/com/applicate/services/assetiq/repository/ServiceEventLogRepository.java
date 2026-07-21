package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceEventLogRepository extends JpaRepository<AiqServiceEventLog, Long> {

    Optional<AiqServiceEventLog> findByTenantIdAndEventTypeAndEventNumber(String tenantId, EventType eventType, String eventNumber);

    List<AiqServiceEventLog> findByTenantIdAndAssetId(String tenantId, Long assetId);

    List<AiqServiceEventLog> findByTenantIdAndStatus(String tenantId, EventStatus status);
}
