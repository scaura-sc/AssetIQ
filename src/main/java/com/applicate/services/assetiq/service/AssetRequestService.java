package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestApproveRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestCreateRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestRejectRequest;
import com.applicate.services.assetiq.dto.assetrequest.AssetRequestResponse;
import com.applicate.services.assetiq.dto.deployment.DeployRequest;
import com.applicate.services.assetiq.dto.deployment.TransferRequest;
import com.applicate.services.assetiq.entity.AiqAssetRequest;
import com.applicate.services.assetiq.entity.enums.AssetRequestStatus;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;
import com.applicate.services.assetiq.exception.ConflictException;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.repository.AssetRequestRepository;
import com.applicate.services.assetiq.util.BusinessCodeGenerator;
import com.applicate.services.assetiq.validation.ActiveAssociationValidator;
import com.applicate.services.assetiq.validation.CatalogHierarchyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * An outlet's request for an asset by category+type, approved from whatever's
 * actually in stock. "In stock" means assetStatus=STOCK AND either no active
 * association at all, or an active association at a WAREHOUSE — that's the
 * normal resting state for inventory (assets don't just float with
 * assetStatus=STOCK and zero association; per this domain, they live in a
 * warehouse until deployed). Approval doesn't duplicate deploy/transfer logic:
 * it picks whichever of AssetDeploymentService.deploy (no prior association)
 * or .transfer (an active warehouse association to move off of) applies, so
 * every precondition is enforced exactly once, not re-checked here.
 */
@Service
@Transactional
public class AssetRequestService {

    private final AssetRequestRepository assetRequestRepository;
    private final AssetRepository assetRepository;
    private final ActiveAssociationValidator activeAssociationValidator;
    private final CatalogHierarchyValidator catalogHierarchyValidator;
    private final AssetDeploymentService assetDeploymentService;

    public AssetRequestService(AssetRequestRepository assetRequestRepository,
                                AssetRepository assetRepository,
                                ActiveAssociationValidator activeAssociationValidator,
                                CatalogHierarchyValidator catalogHierarchyValidator,
                                AssetDeploymentService assetDeploymentService) {
        this.assetRequestRepository = assetRequestRepository;
        this.assetRepository = assetRepository;
        this.activeAssociationValidator = activeAssociationValidator;
        this.catalogHierarchyValidator = catalogHierarchyValidator;
        this.assetDeploymentService = assetDeploymentService;
    }

    public AssetRequestResponse create(AssetRequestCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        catalogHierarchyValidator.validateCategoryTypeChain(tenantId, request.categoryCode(), request.typeCode());

        AiqAssetRequest assetRequest = new AiqAssetRequest();
        assetRequest.setTenantId(tenantId);
        assetRequest.setRequestNumber(BusinessCodeGenerator.generate("AR"));
        assetRequest.setOutletCode(request.outletCode());
        assetRequest.setOutletName(request.outletName());
        assetRequest.setTerritoryCode(request.territoryCode());
        assetRequest.setSalesmanCode(request.salesmanCode());
        assetRequest.setCategoryCode(request.categoryCode());
        assetRequest.setTypeCode(request.typeCode());
        assetRequest.setReason(request.reason());
        assetRequest.setStatus(AssetRequestStatus.PENDING);
        assetRequest.setRequestedByUserCode(request.requestedByUserCode());
        assetRequest.setRequestedAt(request.requestedAt());

        return AssetRequestResponse.from(assetRequestRepository.save(assetRequest));
    }

    public AssetRequestResponse get(Long id) {
        return AssetRequestResponse.from(requireOwned(id));
    }

    public List<AssetRequestResponse> search(AssetRequestStatus status, String outletCode, String territoryCode) {
        String tenantId = TenantContext.getTenantId();
        return assetRequestRepository.findAll(AssetRequestSpecifications.filter(tenantId, status, outletCode, territoryCode))
                .stream().map(AssetRequestResponse::from).toList();
    }

    public List<AssetResponse> listAvailableStock(Long id) {
        String tenantId = TenantContext.getTenantId();
        AiqAssetRequest assetRequest = requireOwned(id);

        return assetRepository.findByTenantIdAndAssetStatusAndCategoryCodeAndTypeCode(
                        tenantId, AssetStatus.STOCK, assetRequest.getCategoryCode(), assetRequest.getTypeCode())
                .stream()
                .filter(asset -> asset.getWorkingStatus() == WorkingStatus.WORKING)
                .filter(asset -> activeAssociationValidator.findActive(tenantId, asset.getId())
                        .map(assoc -> assoc.getLocationType() == LocationType.WAREHOUSE)
                        .orElse(true))
                .map(AssetResponse::from)
                .toList();
    }

    public AssetRequestResponse approve(Long id, AssetRequestApproveRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAssetRequest assetRequest = requireOwned(id);
        if (assetRequest.getStatus() != AssetRequestStatus.PENDING) {
            throw new ConflictException("Asset request " + assetRequest.getRequestNumber() + " is already " + assetRequest.getStatus());
        }

        String reason = request.reason() != null ? request.reason() : assetRequest.getReason();
        boolean hasExistingAssociation = activeAssociationValidator.findActive(tenantId, request.assetId()).isPresent();

        if (hasExistingAssociation) {
            // Warehouse-associated stock — move it off the warehouse association onto the outlet.
            TransferRequest transferRequest = new TransferRequest(
                    LocationType.OUTLET, assetRequest.getOutletCode(), assetRequest.getOutletName(),
                    assetRequest.getTerritoryCode(), assetRequest.getSalesmanCode(), null, null,
                    AssetStatus.DEPLOYED, LocalDate.now(), null, AssignmentType.PERMANENT, null,
                    false, null, null, null, false, null, null, null, null,
                    request.approvedByUserCode(), null, null,
                    reason != null ? reason : "Approved via asset request " + assetRequest.getRequestNumber(),
                    null, null);
            assetDeploymentService.transfer(request.assetId(), transferRequest);
        } else {
            // No prior association at all — genuinely fresh stock.
            DeployRequest deployRequest = new DeployRequest(
                    assetRequest.getOutletCode(), assetRequest.getOutletName(), assetRequest.getTerritoryCode(),
                    assetRequest.getSalesmanCode(), null, null, LocalDate.now(), null, AssignmentType.PERMANENT,
                    null, false, null, null, null, false, null, null, null, null,
                    request.approvedByUserCode(), reason, null, null);
            assetDeploymentService.deploy(request.assetId(), deployRequest);
        }

        assetRequest.setStatus(AssetRequestStatus.APPROVED);
        assetRequest.setApprovedAssetId(request.assetId());
        assetRequest.setApprovedByUserCode(request.approvedByUserCode());
        assetRequest.setApprovedAt(LocalDateTime.now());

        return AssetRequestResponse.from(assetRequestRepository.save(assetRequest));
    }

    public AssetRequestResponse reject(Long id, AssetRequestRejectRequest request) {
        AiqAssetRequest assetRequest = requireOwned(id);
        if (assetRequest.getStatus() != AssetRequestStatus.PENDING) {
            throw new ConflictException("Asset request " + assetRequest.getRequestNumber() + " is already " + assetRequest.getStatus());
        }

        assetRequest.setStatus(AssetRequestStatus.REJECTED);
        assetRequest.setRejectionReason(request.rejectionReason());
        assetRequest.setRejectedByUserCode(request.rejectedByUserCode());
        assetRequest.setRejectedAt(LocalDateTime.now());

        return AssetRequestResponse.from(assetRequestRepository.save(assetRequest));
    }

    private AiqAssetRequest requireOwned(Long id) {
        return assetRequestRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No asset request with id " + id));
    }
}
