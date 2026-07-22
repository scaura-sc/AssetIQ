package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.serviceevent.CloseWorkOrderRequest;
import com.applicate.services.assetiq.dto.serviceevent.ComplaintCreateRequest;
import com.applicate.services.assetiq.dto.serviceevent.MttrByComplaintType;
import com.applicate.services.assetiq.dto.serviceevent.PreventiveMaintenanceRequest;
import com.applicate.services.assetiq.dto.serviceevent.ServiceEventDashboardItem;
import com.applicate.services.assetiq.dto.serviceevent.ServiceEventResponse;
import com.applicate.services.assetiq.dto.serviceevent.WorkOrderCreateRequest;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqServiceEventLog;
import com.applicate.services.assetiq.entity.enums.ComplaintType;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.TriggeredBy;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.repository.ServiceEventLogRepository;
import com.applicate.services.assetiq.util.BusinessCodeGenerator;
import com.applicate.services.assetiq.validation.ReferenceValidationService;
import com.applicate.services.assetiq.validation.ServiceEventValidator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** F14 (complaint), F15 (work order lifecycle), F16 (PM scheduling), F17 (dashboard/SLA/MTTR). */
@Service
@Transactional
public class ServiceEventService {

    private static final int REPEAT_LOOKBACK_DAYS = 30;

    private final ServiceEventLogRepository serviceEventLogRepository;
    private final AssetRepository assetRepository;
    private final ReferenceValidationService referenceValidationService;
    private final ServiceEventValidator serviceEventValidator;

    public ServiceEventService(ServiceEventLogRepository serviceEventLogRepository,
                                AssetRepository assetRepository,
                                ReferenceValidationService referenceValidationService,
                                ServiceEventValidator serviceEventValidator) {
        this.serviceEventLogRepository = serviceEventLogRepository;
        this.assetRepository = assetRepository;
        this.referenceValidationService = referenceValidationService;
        this.serviceEventValidator = serviceEventValidator;
    }

    // ---- F14 Complaint Registration ----

    public ServiceEventResponse createComplaint(ComplaintCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAsset asset = referenceValidationService.requireAsset(tenantId, request.assetId());

        AiqServiceEventLog event = new AiqServiceEventLog();
        event.setTenantId(tenantId);
        event.setEventNumber(BusinessCodeGenerator.generate("CMP"));
        event.setEventType(EventType.COMPLAINT);
        event.setAssetId(request.assetId());
        event.setAssetNumber(asset.getAssetNumber());
        event.setOutletCode(request.outletCode());
        event.setVisitId(request.visitId());
        event.setPriority(request.priority());
        event.setDescription(request.description());
        event.setRaisedByUserCode(request.raisedByUserCode());
        event.setComplaintType(request.complaintType());
        event.setPhotoUrl1(request.photoUrl1());
        event.setPhotoUrl2(request.photoUrl2());
        event.setGpsLat(request.gpsLat());
        event.setGpsLng(request.gpsLng());
        event.setRaisedAt(request.raisedAt());

        event.setIsUnderWarranty(computeIsUnderWarranty(asset, request.raisedAt()));
        event.setIsRepeated(computeIsRepeated(tenantId, request.assetId(), request.complaintType(), request.raisedAt()));

        // DEFERRED(AHS condition score penalty): needs the AHS engine, not built yet.

        serviceEventValidator.validateFieldShape(event);
        return ServiceEventResponse.from(serviceEventLogRepository.save(event));
    }

    private boolean computeIsUnderWarranty(AiqAsset asset, LocalDateTime raisedAt) {
        if (asset.getWarrantyStartDate() == null || asset.getWarrantyEndDate() == null) {
            return false;
        }
        var raisedDate = raisedAt.toLocalDate();
        return !raisedDate.isBefore(asset.getWarrantyStartDate()) && !raisedDate.isAfter(asset.getWarrantyEndDate());
    }

    private boolean computeIsRepeated(String tenantId, Long assetId, ComplaintType complaintType, LocalDateTime raisedAt) {
        LocalDateTime lookbackStart = raisedAt.minusDays(REPEAT_LOOKBACK_DAYS);
        return !serviceEventLogRepository
                .findByTenantIdAndAssetIdAndComplaintTypeAndRaisedAtAfter(tenantId, assetId, complaintType, lookbackStart)
                .isEmpty();
    }

    // ---- F15 Work Order Management ----

    public ServiceEventResponse createWorkOrder(WorkOrderCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAsset asset = referenceValidationService.requireAsset(tenantId, request.assetId());

        AiqServiceEventLog event = new AiqServiceEventLog();
        event.setTenantId(tenantId);
        event.setEventNumber(BusinessCodeGenerator.generate("WO"));
        event.setEventType(EventType.WORK_ORDER);
        event.setAssetId(request.assetId());
        event.setAssetNumber(asset.getAssetNumber());
        event.setOutletCode(request.outletCode());
        event.setPriority(request.priority());
        event.setRaisedByUserCode(request.raisedByUserCode());
        event.setAssignedToUserCode(request.assignedToUserCode());
        event.setWoType(request.woType());
        event.setTriggeredBy(request.triggeredBy());
        event.setPlannedDate(request.plannedDate());
        event.setLabourCost(request.labourCost());
        event.setPartsCost(request.partsCost());
        event.setTotalCost(computeTotalCost(request.labourCost(), request.partsCost()));
        event.setChecklistSummary(request.checklistSummary());
        event.setRaisedAt(request.raisedAt());

        serviceEventValidator.validateFieldShape(event);
        return ServiceEventResponse.from(serviceEventLogRepository.save(event));
    }

    public ServiceEventResponse updateStatus(Long id, EventStatus newStatus) {
        AiqServiceEventLog event = requireOwned(id);
        serviceEventValidator.validateStatusTransition(event.getStatus(), newStatus);
        event.setStatus(newStatus);
        if (newStatus == EventStatus.RESOLVED && event.getResolvedAt() == null) {
            event.setResolvedAt(LocalDateTime.now());
        }
        if (newStatus == EventStatus.IN_PROGRESS && event.getStartedAt() == null) {
            event.setStartedAt(LocalDateTime.now());
        }
        return ServiceEventResponse.from(serviceEventLogRepository.save(event));
    }

    /** F15 closure — photo_after_url/signature_url are enforced by CloseWorkOrderRequest's @NotBlank. */
    public ServiceEventResponse close(Long id, CloseWorkOrderRequest request) {
        AiqServiceEventLog event = requireOwned(id);
        serviceEventValidator.validateStatusTransition(event.getStatus(), EventStatus.CLOSED);

        event.setPhotoAfterUrl(request.photoAfterUrl());
        event.setSignatureUrl(request.signatureUrl());
        if (request.resolutionNotes() != null) {
            event.setResolutionNotes(request.resolutionNotes());
        }
        if (request.labourCost() != null) {
            event.setLabourCost(request.labourCost());
        }
        if (request.partsCost() != null) {
            event.setPartsCost(request.partsCost());
        }
        event.setTotalCost(computeTotalCost(event.getLabourCost(), event.getPartsCost()));
        if (event.getEventType() == EventType.WORK_ORDER && event.getCompletedAt() == null) {
            event.setCompletedAt(LocalDateTime.now());
        }
        event.setStatus(EventStatus.CLOSED);
        event.setClosedAt(LocalDateTime.now());

        // DEFERRED(cost rollup): feed total_cost into aiq_asset_cost_rollup once it exists.

        return ServiceEventResponse.from(serviceEventLogRepository.save(event));
    }

    private BigDecimal computeTotalCost(BigDecimal labourCost, BigDecimal partsCost) {
        if (labourCost == null && partsCost == null) {
            return null;
        }
        BigDecimal labour = labourCost != null ? labourCost : BigDecimal.ZERO;
        BigDecimal parts = partsCost != null ? partsCost : BigDecimal.ZERO;
        return labour.add(parts);
    }

    // ---- F16 Preventive Maintenance Scheduling ----

    public ServiceEventResponse createPreventiveMaintenance(PreventiveMaintenanceRequest request) {
        WorkOrderCreateRequest workOrderRequest = new WorkOrderCreateRequest(
                request.assetId(), request.outletCode(), request.priority(), request.raisedByUserCode(),
                request.assignedToUserCode(), WorkOrderType.PREVENTIVE, TriggeredBy.SCHEDULE, request.plannedDate(),
                null, null, null, request.raisedAt());
        return createWorkOrder(workOrderRequest);
    }

    public List<ServiceEventResponse> listOverduePreventiveMaintenance() {
        return serviceEventLogRepository.findByTenantIdAndEventTypeAndWoTypeAndPlannedDateBeforeAndStartedAtIsNull(
                        TenantContext.getTenantId(), EventType.WORK_ORDER, WorkOrderType.PREVENTIVE, java.time.LocalDate.now())
                .stream().map(ServiceEventResponse::from).toList();
    }

    // ---- F17 Service Dashboard & SLA Tracking ----

    public List<ServiceEventDashboardItem> search(EventType eventType, EventStatus status, Priority priority,
                                                    String territoryCode, String assignedToUserCode,
                                                    LocalDateTime raisedAfter, LocalDateTime raisedBefore) {
        String tenantId = TenantContext.getTenantId();

        // territory isn't a column on this table — resolved via aiq_asset.territory_code instead
        // of a DB join, since there are no FKs to join through anyway.
        List<Long> assetIds = territoryCode != null
                ? assetRepository.findByTenantIdAndTerritoryCode(tenantId, territoryCode).stream().map(AiqAsset::getId).toList()
                : null;

        Specification<AiqServiceEventLog> spec = ServiceEventSpecifications.filter(
                tenantId, eventType, status, priority, assignedToUserCode, raisedAfter, raisedBefore, assetIds);

        return serviceEventLogRepository.findAll(spec).stream().map(this::toDashboardItem).toList();
    }

    private ServiceEventDashboardItem toDashboardItem(AiqServiceEventLog event) {
        LocalDateTime slaDueAt = switch (event.getPriority()) {
            case CRITICAL -> event.getRaisedAt().plusHours(4);
            case HIGH -> event.getRaisedAt().plusHours(24);
            case MEDIUM -> event.getRaisedAt().plusHours(72);
            case LOW -> event.getRaisedAt().plusDays(7);
        };
        LocalDateTime comparisonPoint = event.getResolvedAt() != null ? event.getResolvedAt() : LocalDateTime.now();
        boolean breached = comparisonPoint.isAfter(slaDueAt);
        return new ServiceEventDashboardItem(ServiceEventResponse.from(event), slaDueAt, breached);
    }

    public List<MttrByComplaintType> mttrByComplaintType() {
        List<AiqServiceEventLog> resolved = serviceEventLogRepository
                .findByTenantIdAndEventTypeAndResolvedAtIsNotNull(TenantContext.getTenantId(), EventType.COMPLAINT);

        Map<ComplaintType, List<AiqServiceEventLog>> grouped = resolved.stream()
                .filter(e -> e.getComplaintType() != null)
                .collect(Collectors.groupingBy(AiqServiceEventLog::getComplaintType));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<AiqServiceEventLog> events = entry.getValue();
                    double avgHours = events.stream()
                            .mapToLong(e -> Duration.between(e.getRaisedAt(), e.getResolvedAt()).toMinutes())
                            .average().orElse(0) / 60.0;
                    return new MttrByComplaintType(entry.getKey(), avgHours, events.size());
                })
                .toList();
    }

    // ---- shared reads ----

    public ServiceEventResponse get(Long id) {
        return ServiceEventResponse.from(requireOwned(id));
    }

    public List<ServiceEventResponse> listByAsset(Long assetId) {
        return serviceEventLogRepository.findByTenantIdAndAssetId(TenantContext.getTenantId(), assetId)
                .stream().map(ServiceEventResponse::from).toList();
    }

    private AiqServiceEventLog requireOwned(Long id) {
        return serviceEventLogRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No service event with id " + id));
    }
}
