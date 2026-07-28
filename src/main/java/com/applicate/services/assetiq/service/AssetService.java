package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.asset.AssetCreateRequest;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.util.BusinessCodeGenerator;
import com.applicate.services.assetiq.validation.CatalogHierarchyValidator;
import com.applicate.services.assetiq.validation.ReferenceValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** F02 — Asset Registration (aiq_asset). Deploy/transfer/swap (F05-F07) are added alongside Module 2. */
@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final ReferenceValidationService referenceValidationService;
    private final CatalogHierarchyValidator catalogHierarchyValidator;

    public AssetService(AssetRepository assetRepository,
                         ReferenceValidationService referenceValidationService,
                         CatalogHierarchyValidator catalogHierarchyValidator) {
        this.assetRepository = assetRepository;
        this.referenceValidationService = referenceValidationService;
        this.catalogHierarchyValidator = catalogHierarchyValidator;
    }

    public AssetResponse create(AssetCreateRequest request) {
        String tenantId = TenantContext.getTenantId();

        catalogHierarchyValidator.validateAssetCatalogChain(tenantId, request.categoryCode(), request.typeCode(), request.modelCode());
        referenceValidationService.requireVendor(tenantId, request.vendorCode());
        referenceValidationService.requireVendor(tenantId, request.amcVendorCode());

        if (assetRepository.findByTenantIdAndCategoryCodeAndSerialNumber(tenantId, request.categoryCode(), request.serialNumber()).isPresent()) {
            throw new BadRequestException(
                    "serial_number '" + request.serialNumber() + "' is already registered under category '" + request.categoryCode() + "'");
        }

        AiqAsset asset = new AiqAsset();
        asset.setTenantId(tenantId);
        asset.setAssetNumber(BusinessCodeGenerator.generate("AST"));
        asset.setSerialNumber(request.serialNumber());
        asset.setAssetName(request.assetName());
        asset.setCategoryCode(request.categoryCode());
        asset.setTypeCode(request.typeCode());
        asset.setModelCode(request.modelCode());
        asset.setVendorCode(request.vendorCode());
        asset.setBrandCode(request.brandCode());
        asset.setDivisionCode(request.divisionCode());
        asset.setCompanyCode(request.companyCode());
        asset.setCapacity(request.capacity());
        asset.setCapacityUnit(request.capacityUnit());
        asset.setColour(request.colour());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setPurchasePrice(request.purchasePrice());
        asset.setPurchaseOrderRef(request.purchaseOrderRef());
        asset.setInvoiceRef(request.invoiceRef());
        asset.setManufacturingDate(request.manufacturingDate());
        asset.setWarrantyStartDate(request.warrantyStartDate());
        asset.setWarrantyEndDate(request.warrantyEndDate());
        asset.setWarrantyType(request.warrantyType());
        asset.setAmcStartDate(request.amcStartDate());
        asset.setAmcEndDate(request.amcEndDate());
        asset.setAmcVendorCode(request.amcVendorCode());
        asset.setDepreciationMethod(request.depreciationMethod());
        asset.setUsefulLifeYears(request.usefulLifeYears());
        asset.setResidualValue(request.residualValue());
        // Always STOCK on creation, regardless of any input — there is no asset_status field on
        // AssetCreateRequest at all, so this is the only value this path can ever produce.
        asset.setAssetStatus(AssetStatus.STOCK);
        // A newly registered asset is assumed functional until a complaint/capture says otherwise —
        // without this default, no asset could ever satisfy F05's STOCK+WORKING deploy precondition,
        // since nothing else sets working_status before an asset's first visit/telemetry event.
        asset.setWorkingStatus(WorkingStatus.WORKING);
        asset.setCreatedBy(request.createdBy());
        asset.setUpdatedBy(request.createdBy());

        // Optional initial warehouse placement — neither deploy (outlet-only) nor transfer
        // (requires a pre-existing association to move from) can give a brand-new asset its
        // first warehouse association, so registration itself carries this instead.
        if (request.warehouseCode() != null) {
            asset.setLocationType(LocationType.WAREHOUSE);
            asset.setLocationCode(request.warehouseCode());
            asset.setTerritoryCode(request.territoryCode());
        }

        // TODO(QR/barcode generation): once aiq_qr_registry exists, generate and persist a QR/barcode
        // for this asset here, right after the initial save assigns its id/asset_number.

        return AssetResponse.from(assetRepository.save(asset));
    }

    public AssetResponse get(Long id) {
        return AssetResponse.from(requireOwned(id));
    }

    public List<AssetResponse> list() {
        return assetRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.getTenantId())
                .stream().map(AssetResponse::from).toList();
    }

    AiqAsset requireOwned(Long id) {
        return assetRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No asset with id " + id));
    }
}
