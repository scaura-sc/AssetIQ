package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.catalog.CatalogCreateRequest;
import com.applicate.services.assetiq.dto.catalog.CatalogResponse;
import com.applicate.services.assetiq.dto.catalog.CatalogUpdateRequest;
import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.validation.CatalogHierarchyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** F01 — Asset Classification Setup (aiq_asset_catalog). */
@Service
@Transactional
public class AssetCatalogService {

    private final AssetCatalogRepository assetCatalogRepository;
    private final CatalogHierarchyValidator catalogHierarchyValidator;

    public AssetCatalogService(AssetCatalogRepository assetCatalogRepository,
                                CatalogHierarchyValidator catalogHierarchyValidator) {
        this.assetCatalogRepository = assetCatalogRepository;
        this.catalogHierarchyValidator = catalogHierarchyValidator;
    }

    public CatalogResponse create(CatalogCreateRequest request) {
        String tenantId = TenantContext.getTenantId();

        catalogHierarchyValidator.validateForCreate(tenantId, request.level(), request.parentCode());

        if (assetCatalogRepository.findByTenantIdAndLevelAndCode(tenantId, request.level(), request.code()).isPresent()) {
            throw new BadRequestException(
                    request.level() + " code '" + request.code() + "' already exists for this tenant");
        }

        AiqAssetCatalog entity = new AiqAssetCatalog();
        entity.setTenantId(tenantId);
        entity.setLevel(request.level());
        entity.setCode(request.code());
        entity.setParentCode(request.parentCode());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setManufacturerName(request.manufacturerName());
        entity.setManufacturerCountry(request.manufacturerCountry());
        entity.setManufacturerContactEmail(request.manufacturerContactEmail());
        entity.setManufacturerContactPhone(request.manufacturerContactPhone());
        entity.setDefaultWarrantyMonths(request.defaultWarrantyMonths());
        entity.setDefaultUsefulLifeYears(request.defaultUsefulLifeYears());
        entity.setDefaultDepreciationMethod(request.defaultDepreciationMethod());
        entity.setDefaultPmFrequencyDays(request.defaultPmFrequencyDays());
        entity.setDefaultPurityClausePct(request.defaultPurityClausePct());
        entity.setCapacity(request.capacity());
        entity.setCapacityUnit(request.capacityUnit());

        return CatalogResponse.from(assetCatalogRepository.save(entity));
    }

    public CatalogResponse update(Long id, CatalogUpdateRequest request) {
        AiqAssetCatalog entity = requireOwned(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setManufacturerName(request.manufacturerName());
        entity.setManufacturerCountry(request.manufacturerCountry());
        entity.setManufacturerContactEmail(request.manufacturerContactEmail());
        entity.setManufacturerContactPhone(request.manufacturerContactPhone());
        entity.setDefaultWarrantyMonths(request.defaultWarrantyMonths());
        entity.setDefaultUsefulLifeYears(request.defaultUsefulLifeYears());
        entity.setDefaultDepreciationMethod(request.defaultDepreciationMethod());
        entity.setDefaultPmFrequencyDays(request.defaultPmFrequencyDays());
        entity.setDefaultPurityClausePct(request.defaultPurityClausePct());
        entity.setCapacity(request.capacity());
        entity.setCapacityUnit(request.capacityUnit());
        return CatalogResponse.from(assetCatalogRepository.save(entity));
    }

    public CatalogResponse get(Long id) {
        return CatalogResponse.from(requireOwned(id));
    }

    public List<CatalogResponse> listByLevel(CatalogLevel level) {
        return assetCatalogRepository.findByTenantIdAndLevel(TenantContext.getTenantId(), level).stream()
                .map(CatalogResponse::from).toList();
    }

    public List<CatalogResponse> listChildren(String parentCode) {
        return assetCatalogRepository.findByTenantIdAndParentCode(TenantContext.getTenantId(), parentCode).stream()
                .map(CatalogResponse::from).toList();
    }

    /** "Available models" lookup used elsewhere (e.g. asset registration) — deactivated MODEL rows are excluded. */
    public List<CatalogResponse> listAvailableModels() {
        return assetCatalogRepository.findByTenantIdAndLevelAndIsActiveTrue(TenantContext.getTenantId(), CatalogLevel.MODEL)
                .stream().map(CatalogResponse::from).toList();
    }

    /** Deactivating a CATEGORY (or TYPE) cascade-deactivates all descendant TYPE/MODEL rows — soft delete only. */
    public void deactivate(Long id) {
        String tenantId = TenantContext.getTenantId();
        AiqAssetCatalog entity = requireOwned(id);
        deactivateRecursive(tenantId, entity);
    }

    private void deactivateRecursive(String tenantId, AiqAssetCatalog entity) {
        entity.setIsActive(false);
        assetCatalogRepository.save(entity);
        if (entity.getLevel() != CatalogLevel.MODEL) {
            List<AiqAssetCatalog> children = assetCatalogRepository.findByTenantIdAndParentCode(tenantId, entity.getCode());
            for (AiqAssetCatalog child : children) {
                deactivateRecursive(tenantId, child);
            }
        }
    }

    private AiqAssetCatalog requireOwned(Long id) {
        return assetCatalogRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No catalog entry with id " + id));
    }
}
