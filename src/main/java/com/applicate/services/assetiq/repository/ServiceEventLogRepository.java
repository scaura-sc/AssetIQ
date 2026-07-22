package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceEventLogRepository
        extends JpaRepository<AiqServiceEventLog, Long>, JpaSpecificationExecutor<AiqServiceEventLog> {

    Optional<AiqServiceEventLog> findByTenantIdAndId(String tenantId, Long id);

    Optional<AiqServiceEventLog> findByTenantIdAndEventTypeAndEventNumber(String tenantId, EventType eventType, String eventNumber);

    List<AiqServiceEventLog> findByTenantIdAndAssetId(String tenantId, Long assetId);

    List<AiqServiceEventLog> findByTenantIdAndStatus(String tenantId, EventStatus status);

    /** F14 repeat-complaint detection: same asset + same complaint type, raised within the lookback window. */
    List<AiqServiceEventLog> findByTenantIdAndAssetIdAndComplaintTypeAndRaisedAtAfter(
            String tenantId, Long assetId, ComplaintType complaintType, LocalDateTime after);

    /** F16 overdue PM: planned_date passed, work not yet started. */
    List<AiqServiceEventLog> findByTenantIdAndEventTypeAndWoTypeAndPlannedDateBeforeAndStartedAtIsNull(
            String tenantId, EventType eventType, WorkOrderType woType, LocalDate plannedDateBefore);

    /** F17 MTTR: resolved complaints, aggregated by complaint_type in the service layer. */
    List<AiqServiceEventLog> findByTenantIdAndEventTypeAndResolvedAtIsNotNull(String tenantId, EventType eventType);
}
