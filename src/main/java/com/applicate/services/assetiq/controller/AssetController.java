package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.asset.AssetCreateRequest;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.deployment.DeployRequest;
import com.applicate.services.assetiq.dto.deployment.DeployResponse;
import com.applicate.services.assetiq.dto.deployment.SwapRequest;
import com.applicate.services.assetiq.dto.deployment.SwapResponse;
import com.applicate.services.assetiq.dto.deployment.TransferRequest;
import com.applicate.services.assetiq.dto.deployment.TransferResponse;
import com.applicate.services.assetiq.service.AssetDeploymentService;
import com.applicate.services.assetiq.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** F02 (registration) + F05/F06/F07 (deploy/transfer/swap). tenant_id via X-Tenant-Id header. */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetDeploymentService assetDeploymentService;

    public AssetController(AssetService assetService, AssetDeploymentService assetDeploymentService) {
        this.assetService = assetService;
        this.assetDeploymentService = assetDeploymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetCreateRequest request) {
        return assetService.create(request);
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable Long id) {
        return assetService.get(id);
    }

    @PostMapping("/{id}/deploy")
    public DeployResponse deploy(@PathVariable Long id, @Valid @RequestBody DeployRequest request) {
        return assetDeploymentService.deploy(id, request);
    }

    @PostMapping("/{id}/transfer")
    public TransferResponse transfer(@PathVariable Long id, @Valid @RequestBody TransferRequest request) {
        return assetDeploymentService.transfer(id, request);
    }

    @PostMapping("/swap")
    public SwapResponse swap(@Valid @RequestBody SwapRequest request) {
        return assetDeploymentService.swap(request);
    }
}
