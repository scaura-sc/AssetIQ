package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.TriggeredBy;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceEventLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ServiceEventLogRepository serviceEventLogRepository;

    @Test
    void persistsComplaintWithWorkOrderColumnsNull() {
        AiqServiceEventLog complaint = new AiqServiceEventLog();
        complaint.setTenantId("tenant-1");
        complaint.setEventNumber("CMP-0001");
        complaint.setEventType(EventType.COMPLAINT);
        complaint.setAssetId(1001L);
        complaint.setAssetNumber("AST-0001");
        complaint.setOutletCode("OUT-100");
        complaint.setPriority(Priority.HIGH);
        complaint.setRaisedByUserCode("user-1");
        complaint.setComplaintType(ComplaintType.NOT_WORKING);
        complaint.setRaisedAt(LocalDateTime.of(2025, 4, 1, 9, 0));

        AiqServiceEventLog saved = serviceEventLogRepository.saveAndFlush(complaint);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(EventStatus.OPEN);
        assertThat(saved.getIsUnderWarranty()).isFalse();
        assertThat(saved.getWoType()).isNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsWorkOrderWithCostRollup() {
        AiqServiceEventLog workOrder = new AiqServiceEventLog();
        workOrder.setTenantId("tenant-1");
        workOrder.setEventNumber("WO-0001");
        workOrder.setEventType(EventType.WORK_ORDER);
        workOrder.setAssetId(1002L);
        workOrder.setAssetNumber("AST-0002");
        workOrder.setOutletCode("OUT-101");
        workOrder.setPriority(Priority.MEDIUM);
        workOrder.setRaisedByUserCode("user-2");
        workOrder.setWoType(WorkOrderType.PREVENTIVE);
        workOrder.setTriggeredBy(TriggeredBy.SCHEDULE);
        workOrder.setPlannedDate(LocalDate.of(2025, 4, 10));
        workOrder.setLabourCost(new BigDecimal("500.00"));
        workOrder.setPartsCost(new BigDecimal("250.00"));
        workOrder.setTotalCost(new BigDecimal("750.00"));
        workOrder.setRaisedAt(LocalDateTime.of(2025, 4, 1, 9, 0));

        AiqServiceEventLog saved = serviceEventLogRepository.saveAndFlush(workOrder);
        assertThat(saved.getTotalCost()).isEqualByComparingTo("750.00");

        Optional<AiqServiceEventLog> reloaded =
                serviceEventLogRepository.findByTenantIdAndEventTypeAndEventNumber("tenant-1", EventType.WORK_ORDER, "WO-0001");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getComplaintType()).isNull();
    }

    @Test
    void findsByAssetAndStatus() {
        AiqServiceEventLog event = new AiqServiceEventLog();
        event.setTenantId("tenant-2");
        event.setEventNumber("CMP-0002");
        event.setEventType(EventType.COMPLAINT);
        event.setAssetId(2002L);
        event.setAssetNumber("AST-0003");
        event.setOutletCode("OUT-200");
        event.setPriority(Priority.LOW);
        event.setRaisedByUserCode("user-3");
        event.setRaisedAt(LocalDateTime.of(2025, 4, 2, 9, 0));
        serviceEventLogRepository.saveAndFlush(event);

        List<AiqServiceEventLog> byAsset = serviceEventLogRepository.findByTenantIdAndAssetId("tenant-2", 2002L);
        assertThat(byAsset).hasSize(1);

        List<AiqServiceEventLog> open = serviceEventLogRepository.findByTenantIdAndStatus("tenant-2", EventStatus.OPEN);
        assertThat(open).hasSize(1);
    }
}
