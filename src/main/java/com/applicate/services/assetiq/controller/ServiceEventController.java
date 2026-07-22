package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.serviceevent.AssignRequest;
import com.applicate.services.assetiq.dto.serviceevent.CloseWorkOrderRequest;
import com.applicate.services.assetiq.dto.serviceevent.ComplaintCreateRequest;
import com.applicate.services.assetiq.dto.serviceevent.MttrByComplaintType;
import com.applicate.services.assetiq.dto.serviceevent.PreventiveMaintenanceRequest;
import com.applicate.services.assetiq.dto.serviceevent.ServiceEventDashboardItem;
import com.applicate.services.assetiq.dto.serviceevent.ServiceEventResponse;
import com.applicate.services.assetiq.dto.serviceevent.StatusUpdateRequest;
import com.applicate.services.assetiq.dto.serviceevent.WorkOrderCreateRequest;
import com.applicate.services.assetiq.entity.enums.EventStatus;
import com.applicate.services.assetiq.entity.enums.EventType;
import com.applicate.services.assetiq.entity.enums.Priority;
import com.applicate.services.assetiq.entity.enums.WorkOrderType;
import com.applicate.services.assetiq.service.ServiceEventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** F14 (complaint), F15 (work order), F16 (PM scheduling), F17 (dashboard/SLA/MTTR). */
@RestController
@RequestMapping("/api/service-events")
public class ServiceEventController {

    private final ServiceEventService serviceEventService;

    public ServiceEventController(ServiceEventService serviceEventService) {
        this.serviceEventService = serviceEventService;
    }

    @PostMapping("/complaints")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceEventResponse createComplaint(@Valid @RequestBody ComplaintCreateRequest request) {
        return serviceEventService.createComplaint(request);
    }

    @PostMapping("/work-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceEventResponse createWorkOrder(@Valid @RequestBody WorkOrderCreateRequest request) {
        return serviceEventService.createWorkOrder(request);
    }

    @PostMapping("/work-orders/preventive-maintenance")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceEventResponse createPreventiveMaintenance(@Valid @RequestBody PreventiveMaintenanceRequest request) {
        return serviceEventService.createPreventiveMaintenance(request);
    }

    @GetMapping("/work-orders/overdue")
    public List<ServiceEventResponse> listOverduePreventiveMaintenance() {
        return serviceEventService.listOverduePreventiveMaintenance();
    }

    @PatchMapping("/{id}/status")
    public ServiceEventResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return serviceEventService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/assign")
    public ServiceEventResponse assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return serviceEventService.assign(id, request.assignedToUserCode());
    }

    @PostMapping("/{id}/close")
    public ServiceEventResponse close(@PathVariable Long id, @Valid @RequestBody CloseWorkOrderRequest request) {
        return serviceEventService.close(id, request);
    }

    @GetMapping("/search")
    public List<ServiceEventDashboardItem> search(
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String outletCode,
            @RequestParam(required = false) WorkOrderType woType,
            @RequestParam(required = false) String territoryCode,
            @RequestParam(required = false) String assignedToUserCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime raisedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime raisedBefore) {
        return serviceEventService.search(eventType, status, priority, outletCode, woType, territoryCode, assignedToUserCode, raisedAfter, raisedBefore);
    }

    @GetMapping("/mttr")
    public List<MttrByComplaintType> mttr() {
        return serviceEventService.mttrByComplaintType();
    }

    @GetMapping("/{id}")
    public ServiceEventResponse get(@PathVariable Long id) {
        return serviceEventService.get(id);
    }

    @GetMapping
    public List<ServiceEventResponse> listByAsset(@RequestParam Long assetId) {
        return serviceEventService.listByAsset(assetId);
    }
}
