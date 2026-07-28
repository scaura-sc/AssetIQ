package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.bulkupload.BulkUploadResult;
import com.applicate.services.assetiq.dto.catalog.CatalogCreateRequest;
import com.applicate.services.assetiq.dto.catalog.CatalogResponse;
import com.applicate.services.assetiq.dto.catalog.CatalogUpdateRequest;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.service.AssetCatalogService;
import com.applicate.services.assetiq.service.CatalogBulkUploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** F01 — Asset Classification Setup. tenant_id comes from the X-Tenant-Id header (see TenantFilter). */
@RestController
@RequestMapping("/api/asset-catalog")
public class AssetCatalogController {

    private final AssetCatalogService assetCatalogService;
    private final CatalogBulkUploadService catalogBulkUploadService;

    public AssetCatalogController(AssetCatalogService assetCatalogService,
                                   CatalogBulkUploadService catalogBulkUploadService) {
        this.assetCatalogService = assetCatalogService;
        this.catalogBulkUploadService = catalogBulkUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogResponse create(@Valid @RequestBody CatalogCreateRequest request) {
        return assetCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogUpdateRequest request) {
        return assetCatalogService.update(id, request);
    }

    @GetMapping("/{id}")
    public CatalogResponse get(@PathVariable Long id) {
        return assetCatalogService.get(id);
    }

    @GetMapping
    public List<CatalogResponse> listByLevel(@RequestParam CatalogLevel level) {
        return assetCatalogService.listByLevel(level);
    }

    @GetMapping("/children")
    public List<CatalogResponse> listChildren(@RequestParam String parentCode) {
        return assetCatalogService.listChildren(parentCode);
    }

    @GetMapping("/models/available")
    public List<CatalogResponse> listAvailableModels() {
        return assetCatalogService.listAvailableModels();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        assetCatalogService.deactivate(id);
    }

    @PostMapping("/bulk-upload")
    public BulkUploadResult bulkUpload(@RequestPart("file") MultipartFile file) {
        return catalogBulkUploadService.upload(file);
    }

    @GetMapping("/bulk-upload/template")
    public ResponseEntity<byte[]> bulkUploadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=asset-catalog-template.xlsx")
                .body(catalogBulkUploadService.buildTemplate());
    }
}
