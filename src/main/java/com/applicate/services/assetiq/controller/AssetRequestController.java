package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestApproveRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestCreateRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestRejectRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestResponse;
import com.applicate.services.assetiq.entity.enums.AssetRequestStatus;
import com.applicate.services.assetiq.service.AssetRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** An outlet's request for an asset by category+type, approved from stock. */
@RestController
@RequestMapping("/api/asset-requests")
public class AssetRequestController {

    private final AssetRequestService assetRequestService;

    public AssetRequestController(AssetRequestService assetRequestService) {
        this.assetRequestService = assetRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetRequestResponse create(@Valid @RequestBody AssetRequestCreateRequest request) {
        return assetRequestService.create(request);
    }

    @GetMapping("/{id}")
    public AssetRequestResponse get(@PathVariable Long id) {
        return assetRequestService.get(id);
    }

    @GetMapping
    public List<AssetRequestResponse> search(
            @RequestParam(required = false) AssetRequestStatus status,
            @RequestParam(required = false) String outletCode,
            @RequestParam(required = false) String territoryCode) {
        return assetRequestService.search(status, outletCode, territoryCode);
    }

    @GetMapping("/{id}/available-stock")
    public List<AssetResponse> availableStock(@PathVariable Long id) {
        return assetRequestService.listAvailableStock(id);
    }

    @PostMapping("/{id}/approve")
    public AssetRequestResponse approve(@PathVariable Long id, @Valid @RequestBody AssetRequestApproveRequest request) {
        return assetRequestService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public AssetRequestResponse reject(@PathVariable Long id, @Valid @RequestBody AssetRequestRejectRequest request) {
        return assetRequestService.reject(id, request);
    }
}
