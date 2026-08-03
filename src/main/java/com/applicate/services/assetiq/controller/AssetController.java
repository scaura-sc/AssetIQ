package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.asset.AssetCreateRequest;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.association.AssociationResponse;
import com.applicate.services.assetiq.dto.bulkupload.BulkUploadResult;
import com.applicate.services.assetiq.dto.deployment.DeployRequest;
import com.applicate.services.assetiq.dto.deployment.DeployResponse;
import com.applicate.services.assetiq.dto.deployment.ReceiveRequest;
import com.applicate.services.assetiq.dto.deployment.ReceiveResponse;
import com.applicate.services.assetiq.dto.deployment.SwapRequest;
import com.applicate.services.assetiq.dto.deployment.SwapResponse;
import com.applicate.services.assetiq.dto.deployment.TransferRequest;
import com.applicate.services.assetiq.dto.deployment.TransferResponse;
import com.applicate.services.assetiq.dto.fleet.FleetSnapshotResponse;
import com.applicate.services.assetiq.dto.movement.MovementLogResponse;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.service.AssetBulkUploadService;
import com.applicate.services.assetiq.service.AssetDeploymentService;
import com.applicate.services.assetiq.service.AssetService;
import com.applicate.services.assetiq.service.FleetSnapshotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/** F02 (registration) + F05/F06/F07 (deploy/transfer/swap). tenant_id via X-Tenant-Id header. */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetDeploymentService assetDeploymentService;
    private final AssetBulkUploadService assetBulkUploadService;
    private final FleetSnapshotService fleetSnapshotService;

    public AssetController(AssetService assetService, AssetDeploymentService assetDeploymentService,
                            AssetBulkUploadService assetBulkUploadService, FleetSnapshotService fleetSnapshotService) {
        this.assetService = assetService;
        this.assetDeploymentService = assetDeploymentService;
        this.assetBulkUploadService = assetBulkUploadService;
        this.fleetSnapshotService = fleetSnapshotService;
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

    @GetMapping
    public List<AssetResponse> list() {
        return assetService.list();
    }

    /** Fleet-wide, paginated view pre-joined with current association + latest capture — see
     *  docs/backend-requirements-fleet-snapshot.md (AssetIQ-Dashboard, commit a86f460). */
    @GetMapping("/fleet-snapshot")
    public FleetSnapshotResponse fleetSnapshot(
            @RequestParam(required = false) String territoryCode,
            @RequestParam(required = false) String outletCode,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) AssetStatus assetStatus,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        // outletCode/locationCode are the same filter (Asset.locationCode) under two names, per the
        // frontend doc's query param table — locationCode wins if a caller somehow sends both.
        String effectiveLocationCode = locationCode != null ? locationCode : outletCode;
        return fleetSnapshotService.snapshot(territoryCode, effectiveLocationCode, assetStatus, modelCode, search, page, size);
    }

    /** Date-bounded movement activity feed across assets — a different access pattern from
     *  /{id}/movements (per-asset full history), see the frontend doc referenced above. */
    @GetMapping("/movements/search")
    public List<MovementLogResponse> searchMovements(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String territoryCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return assetDeploymentService.searchMovements(assetId, territoryCode, from, to);
    }

    @PostMapping("/{id}/deploy")
    public DeployResponse deploy(@PathVariable Long id, @Valid @RequestBody DeployRequest request) {
        return assetDeploymentService.deploy(id, request);
    }

    @PostMapping("/{id}/receive")
    public ReceiveResponse receive(@PathVariable Long id, @Valid @RequestBody ReceiveRequest request) {
        return assetDeploymentService.receive(id, request);
    }

    @PostMapping("/{id}/transfer")
    public TransferResponse transfer(@PathVariable Long id, @Valid @RequestBody TransferRequest request) {
        return assetDeploymentService.transfer(id, request);
    }

    @PostMapping("/swap")
    public SwapResponse swap(@Valid @RequestBody SwapRequest request) {
        return assetDeploymentService.swap(request);
    }

    @GetMapping("/{id}/associations/current")
    public ResponseEntity<AssociationResponse> getCurrentAssociation(@PathVariable Long id) {
        AssociationResponse current = assetDeploymentService.getCurrentAssociation(id);
        return current != null ? ResponseEntity.ok(current) : ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/associations")
    public List<AssociationResponse> listAssociationHistory(@PathVariable Long id) {
        return assetDeploymentService.listAssociationHistory(id);
    }

    @GetMapping("/{id}/movements")
    public List<MovementLogResponse> listMovementHistory(@PathVariable Long id) {
        return assetDeploymentService.listMovementHistory(id);
    }

    @PostMapping("/bulk-upload")
    public BulkUploadResult bulkUpload(@RequestPart("file") MultipartFile file, @RequestParam String createdBy) {
        return assetBulkUploadService.upload(file, createdBy);
    }

    @GetMapping("/bulk-upload/template")
    public ResponseEntity<byte[]> bulkUploadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assets-template.xlsx")
                .body(assetBulkUploadService.buildTemplate());
    }
}
