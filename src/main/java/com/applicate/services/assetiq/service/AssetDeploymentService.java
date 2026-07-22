package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.association.AssociationResponse;
import com.applicate.services.assetiq.dto.asset.AssetResponse;
import com.applicate.services.assetiq.dto.deployment.DeployRequest;
import com.applicate.services.assetiq.dto.deployment.DeployResponse;
import com.applicate.services.assetiq.dto.deployment.SwapRequest;
import com.applicate.services.assetiq.dto.deployment.SwapResponse;
import com.applicate.services.assetiq.dto.deployment.TransferRequest;
import com.applicate.services.assetiq.dto.deployment.TransferResponse;
import com.applicate.services.assetiq.dto.movement.MovementLogResponse;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.ConflictException;
import com.applicate.services.assetiq.repository.AssetAssociationRepository;
import com.applicate.services.assetiq.repository.AssetMovementLogRepository;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.util.BusinessCodeGenerator;
import com.applicate.services.assetiq.validation.ActiveAssociationValidator;
import com.applicate.services.assetiq.validation.AssetMovementValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/** F05 (deploy), F06 (transfer), F07 (swap) — asset deployment and movement. */
@Service
@Transactional
public class AssetDeploymentService {

    private static final Set<AssetStatus> TRANSFER_TARGET_STATUSES =
            EnumSet.of(AssetStatus.DEPLOYED, AssetStatus.UNDER_REPAIR, AssetStatus.STOCK, AssetStatus.RETIRED, AssetStatus.SCRAPPED);

    private final AssetRepository assetRepository;
    private final AssetAssociationRepository assetAssociationRepository;
    private final AssetMovementLogRepository assetMovementLogRepository;
    private final AssetService assetService;
    private final ActiveAssociationValidator activeAssociationValidator;
    private final AssetMovementValidator assetMovementValidator;

    public AssetDeploymentService(AssetRepository assetRepository,
                                   AssetAssociationRepository assetAssociationRepository,
                                   AssetMovementLogRepository assetMovementLogRepository,
                                   AssetService assetService,
                                   ActiveAssociationValidator activeAssociationValidator,
                                   AssetMovementValidator assetMovementValidator) {
        this.assetRepository = assetRepository;
        this.assetAssociationRepository = assetAssociationRepository;
        this.assetMovementLogRepository = assetMovementLogRepository;
        this.assetService = assetService;
        this.activeAssociationValidator = activeAssociationValidator;
        this.assetMovementValidator = assetMovementValidator;
    }

    // ---- F05 Asset Deployment to Outlet ----

    public DeployResponse deploy(Long assetId, DeployRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAsset asset = assetService.requireOwned(assetId);

        assetMovementValidator.validateDeployable(asset);
        activeAssociationValidator.validateNoActiveAssociation(tenantId, assetId);

        AiqAssetAssociation association = new AiqAssetAssociation();
        association.setTenantId(tenantId);
        association.setAssetId(assetId);
        association.setAssetNumber(asset.getAssetNumber());
        association.setLocationType(LocationType.OUTLET);
        association.setLocationCode(request.outletCode());
        association.setLocationName(request.outletName());
        association.setTerritoryCode(request.territoryCode());
        association.setSalesmanCode(request.salesmanCode());
        association.setCustodianName(request.custodianName());
        association.setCustodianPhone(request.custodianPhone());
        association.setAssignmentDate(request.assignmentDate());
        association.setExpectedReturnDate(request.expectedReturnDate());
        association.setAssignmentType(request.assignmentType() != null ? request.assignmentType() : AssignmentType.PERMANENT);
        association.setAssignmentRef(request.assignmentRef());
        association.setHasContract(request.hasContract());
        association.setContractRef(request.contractRef());
        association.setDepositAmount(request.depositAmount());
        association.setDepositStatus(request.depositStatus());
        association.setExclusivityFlag(request.exclusivityFlag());
        // If no contract purity_clause_pct is provided, this stays null — falling back to the
        // model's default_purity_clause_pct is an AHS-engine concern, not implemented here.
        association.setPurityClausePct(request.purityClausePct());
        association.setContractStartDate(request.contractStartDate());
        association.setContractEndDate(request.contractEndDate());
        association.setContractDocumentUrl(request.contractDocumentUrl());
        association.setCreatedBy(request.movedByUserCode());
        association = assetAssociationRepository.save(association);

        AiqAssetMovementLog movement = new AiqAssetMovementLog();
        movement.setTenantId(tenantId);
        movement.setAssetId(assetId);
        movement.setAssetNumber(asset.getAssetNumber());
        movement.setMovementType(MovementType.ASSIGN);
        movement.setToLocationType(LocationType.OUTLET);
        movement.setToLocationCode(request.outletCode());
        movement.setMovedByUserCode(request.movedByUserCode());
        movement.setReason(request.reason());
        // DEFERRED: validate gpsLat/gpsLng against the outlet's registered GPS once
        // aiq_outlet_ref_cache exists. Accepted as freeform input for now.
        movement.setGpsLat(request.gpsLat());
        movement.setGpsLng(request.gpsLng());
        movement.setMovedAt(LocalDateTime.now());
        movement = assetMovementLogRepository.save(movement);

        asset.setAssetStatus(AssetStatus.DEPLOYED);
        asset.setLocationType(LocationType.OUTLET);
        asset.setLocationCode(request.outletCode());
        asset.setTerritoryCode(request.territoryCode());
        asset.setSalesmanCode(request.salesmanCode());
        asset.setUpdatedBy(request.movedByUserCode());
        asset = assetRepository.save(asset);

        // TODO(stock summary): once aiq_asset_stock_summary exists, decrement this
        // model/category's stock count here.

        return new DeployResponse(AssetResponse.from(asset), AssociationResponse.from(association), MovementLogResponse.from(movement));
    }

    // ---- F06 Asset Transfer Between Locations ----

    public TransferResponse transfer(Long assetId, TransferRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAsset asset = assetService.requireOwned(assetId);

        assetMovementValidator.validateNotTerminal(asset);
        if (!TRANSFER_TARGET_STATUSES.contains(request.targetAssetStatus())) {
            throw new BadRequestException("target_asset_status must be one of " + TRANSFER_TARGET_STATUSES);
        }

        AiqAssetAssociation current = activeAssociationValidator.findActive(tenantId, assetId)
                .orElseThrow(() -> new ConflictException(
                        "Asset " + asset.getAssetNumber() + " has no current active association to transfer from"));

        assetMovementValidator.validateTransferFromOutletAllowed(tenantId, asset, current);

        current.setIsActive(false);
        assetAssociationRepository.save(current);

        activeAssociationValidator.validateNoActiveAssociation(tenantId, assetId);
        AiqAssetAssociation destination = new AiqAssetAssociation();
        destination.setTenantId(tenantId);
        destination.setAssetId(assetId);
        destination.setAssetNumber(asset.getAssetNumber());
        destination.setLocationType(request.toLocationType());
        destination.setLocationCode(request.toLocationCode());
        destination.setLocationName(request.toLocationName());
        destination.setTerritoryCode(request.territoryCode());
        destination.setSalesmanCode(request.salesmanCode());
        destination.setCustodianName(request.custodianName());
        destination.setCustodianPhone(request.custodianPhone());
        destination.setAssignmentDate(request.assignmentDate());
        destination.setExpectedReturnDate(request.expectedReturnDate());
        destination.setAssignmentType(request.assignmentType() != null ? request.assignmentType() : AssignmentType.PERMANENT);
        destination.setAssignmentRef(request.assignmentRef());
        destination.setHasContract(request.hasContract());
        destination.setContractRef(request.contractRef());
        destination.setDepositAmount(request.depositAmount());
        destination.setDepositStatus(request.depositStatus());
        destination.setExclusivityFlag(request.exclusivityFlag());
        destination.setPurityClausePct(request.purityClausePct());
        destination.setContractStartDate(request.contractStartDate());
        destination.setContractEndDate(request.contractEndDate());
        destination.setContractDocumentUrl(request.contractDocumentUrl());
        destination.setCreatedBy(request.movedByUserCode());
        destination = assetAssociationRepository.save(destination);

        AiqAssetMovementLog movement = new AiqAssetMovementLog();
        movement.setTenantId(tenantId);
        movement.setAssetId(assetId);
        movement.setAssetNumber(asset.getAssetNumber());
        movement.setMovementType(MovementType.TRANSFER);
        movement.setFromLocationType(current.getLocationType());
        movement.setFromLocationCode(current.getLocationCode());
        movement.setToLocationType(request.toLocationType());
        movement.setToLocationCode(request.toLocationCode());
        movement.setMovedByUserCode(request.movedByUserCode());
        movement.setApprovedByUserCode(request.approvedByUserCode());
        movement.setApprovalRef(request.approvalRef());
        movement.setReason(request.reason());
        movement.setGpsLat(request.gpsLat());
        movement.setGpsLng(request.gpsLng());
        movement.setMovedAt(LocalDateTime.now());
        movement = assetMovementLogRepository.save(movement);

        asset.setAssetStatus(request.targetAssetStatus());
        asset.setLocationType(request.toLocationType());
        asset.setLocationCode(request.toLocationCode());
        asset.setTerritoryCode(request.territoryCode());
        asset.setSalesmanCode(request.salesmanCode());
        asset.setUpdatedBy(request.movedByUserCode());
        assetRepository.save(asset);

        // Reject any further movement/deployment: enforced on the NEXT call via
        // AssetMovementValidator.validateNotTerminal, not here — RETIRED/SCRAPPED is
        // now persisted and this transfer itself is not "further" movement.

        return new TransferResponse(AssetResponse.from(asset), AssociationResponse.from(destination), MovementLogResponse.from(movement));
    }

    // ---- F07 Asset Swap ----

    public SwapResponse swap(SwapRequest request) {
        String tenantId = TenantContext.getTenantId();
        AiqAsset oldAsset = assetService.requireOwned(request.oldAssetId());
        AiqAsset newAsset = assetService.requireOwned(request.newAssetId());

        assetMovementValidator.validateNotTerminal(oldAsset);
        assetMovementValidator.validateNotTerminal(newAsset);
        assetMovementValidator.validateDeployable(newAsset);

        AiqAssetAssociation oldAssociation = activeAssociationValidator.findActive(tenantId, oldAsset.getId())
                .orElseThrow(() -> new ConflictException(
                        "Asset " + oldAsset.getAssetNumber() + " has no active association to swap out of"));

        String swapReference = request.swapReference() != null
                ? request.swapReference()
                : BusinessCodeGenerator.generate("SWAP");
        LocalDateTime now = LocalDateTime.now();

        oldAssociation.setIsActive(false);
        assetAssociationRepository.save(oldAssociation);

        AiqAssetMovementLog swapOut = new AiqAssetMovementLog();
        swapOut.setTenantId(tenantId);
        swapOut.setAssetId(oldAsset.getId());
        swapOut.setAssetNumber(oldAsset.getAssetNumber());
        swapOut.setMovementType(MovementType.SWAP_OUT);
        swapOut.setFromLocationType(oldAssociation.getLocationType());
        swapOut.setFromLocationCode(oldAssociation.getLocationCode());
        swapOut.setMovedByUserCode(request.movedByUserCode());
        swapOut.setApprovalRef(swapReference);
        swapOut.setReason(request.reason());
        swapOut.setGpsLat(request.gpsLat());
        swapOut.setGpsLng(request.gpsLng());
        swapOut.setMovedAt(now);
        swapOut = assetMovementLogRepository.save(swapOut);

        oldAsset.setAssetStatus(AssetStatus.UNDER_REPAIR);
        oldAsset.setUpdatedBy(request.movedByUserCode());
        assetRepository.save(oldAsset);

        activeAssociationValidator.validateNoActiveAssociation(tenantId, newAsset.getId());
        AiqAssetAssociation newAssociation = new AiqAssetAssociation();
        newAssociation.setTenantId(tenantId);
        newAssociation.setAssetId(newAsset.getId());
        newAssociation.setAssetNumber(newAsset.getAssetNumber());
        newAssociation.setLocationType(oldAssociation.getLocationType());
        newAssociation.setLocationCode(oldAssociation.getLocationCode());
        newAssociation.setLocationName(oldAssociation.getLocationName());
        newAssociation.setTerritoryCode(oldAssociation.getTerritoryCode());
        newAssociation.setSalesmanCode(oldAssociation.getSalesmanCode());
        newAssociation.setCustodianName(oldAssociation.getCustodianName());
        newAssociation.setCustodianPhone(oldAssociation.getCustodianPhone());
        newAssociation.setAssignmentDate(now.toLocalDate());
        newAssociation.setAssignmentType(oldAssociation.getAssignmentType());
        // Contract terms carry over from the slot being replaced — same outlet, same commercial
        // arrangement, only the physical unit changes.
        newAssociation.setHasContract(oldAssociation.getHasContract());
        newAssociation.setContractRef(oldAssociation.getContractRef());
        newAssociation.setDepositAmount(oldAssociation.getDepositAmount());
        newAssociation.setDepositStatus(oldAssociation.getDepositStatus());
        newAssociation.setExclusivityFlag(oldAssociation.getExclusivityFlag());
        newAssociation.setPurityClausePct(oldAssociation.getPurityClausePct());
        newAssociation.setContractStartDate(oldAssociation.getContractStartDate());
        newAssociation.setContractEndDate(oldAssociation.getContractEndDate());
        newAssociation.setContractDocumentUrl(oldAssociation.getContractDocumentUrl());
        newAssociation.setCreatedBy(request.movedByUserCode());
        newAssociation = assetAssociationRepository.save(newAssociation);

        AiqAssetMovementLog swapIn = new AiqAssetMovementLog();
        swapIn.setTenantId(tenantId);
        swapIn.setAssetId(newAsset.getId());
        swapIn.setAssetNumber(newAsset.getAssetNumber());
        swapIn.setMovementType(MovementType.SWAP_IN);
        swapIn.setFromLocationType(LocationType.STOCK);
        swapIn.setToLocationType(oldAssociation.getLocationType());
        swapIn.setToLocationCode(oldAssociation.getLocationCode());
        swapIn.setMovedByUserCode(request.movedByUserCode());
        swapIn.setApprovalRef(swapReference);
        swapIn.setReason(request.reason());
        swapIn.setGpsLat(request.gpsLat());
        swapIn.setGpsLng(request.gpsLng());
        swapIn.setMovedAt(now);
        swapIn = assetMovementLogRepository.save(swapIn);

        newAsset.setAssetStatus(AssetStatus.DEPLOYED);
        newAsset.setLocationType(oldAssociation.getLocationType());
        newAsset.setLocationCode(oldAssociation.getLocationCode());
        newAsset.setTerritoryCode(oldAssociation.getTerritoryCode());
        newAsset.setSalesmanCode(oldAssociation.getSalesmanCode());
        newAsset.setUpdatedBy(request.movedByUserCode());
        newAsset = assetRepository.save(newAsset);

        return new SwapResponse(
                AssetResponse.from(oldAsset), AssetResponse.from(newAsset), AssociationResponse.from(newAssociation),
                MovementLogResponse.from(swapOut), MovementLogResponse.from(swapIn), swapReference);
    }
}
