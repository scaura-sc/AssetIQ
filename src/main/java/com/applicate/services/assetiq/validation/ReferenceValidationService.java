package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqAiVisionResultLog;
import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.AiqVendor;
import com.applicate.services.assetiq.entity.AiqVisitAssetCapture;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.repository.AiVisionResultLogRepository;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.repository.AssetRepository;
import com.applicate.services.assetiq.repository.RoleConfigRepository;
import com.applicate.services.assetiq.repository.VendorRepository;
import com.applicate.services.assetiq.repository.VisitAssetCaptureRepository;
import org.springframework.stereotype.Service;

/**
 * Resolves every soft-reference column used across the 10 entities (there
 * are no DB-level foreign keys, so this is the one place that checks a
 * referenced code/id actually exists and is active). Every service method
 * that accepts a soft-reference from a request must call the matching
 * {@code requireX} method here before persisting — never re-implement the
 * lookup/existence check locally.
 *
 * <p>These all throw {@link BadRequestException} (400): a soft-reference
 * supplied in a request payload that doesn't resolve is a client input
 * error, not a missing-primary-resource (404) or state-conflict (409) case.
 */
@Service
public class ReferenceValidationService {

    private final AssetCatalogRepository assetCatalogRepository;
    private final VendorRepository vendorRepository;
    private final AssetRepository assetRepository;
    private final VisitAssetCaptureRepository visitAssetCaptureRepository;
    private final AiVisionResultLogRepository aiVisionResultLogRepository;
    private final RoleConfigRepository roleConfigRepository;

    public ReferenceValidationService(AssetCatalogRepository assetCatalogRepository,
                                       VendorRepository vendorRepository,
                                       AssetRepository assetRepository,
                                       VisitAssetCaptureRepository visitAssetCaptureRepository,
                                       AiVisionResultLogRepository aiVisionResultLogRepository,
                                       RoleConfigRepository roleConfigRepository) {
        this.assetCatalogRepository = assetCatalogRepository;
        this.vendorRepository = vendorRepository;
        this.assetRepository = assetRepository;
        this.visitAssetCaptureRepository = visitAssetCaptureRepository;
        this.aiVisionResultLogRepository = aiVisionResultLogRepository;
        this.roleConfigRepository = roleConfigRepository;
    }

    public AiqAssetCatalog requireCatalogEntry(String tenantId, CatalogLevel level, String code) {
        return assetCatalogRepository.findByTenantIdAndLevelAndCode(tenantId, level, code)
                .filter(AiqAssetCatalog::getIsActive)
                .orElseThrow(() -> new BadRequestException(
                        "No active " + level + " catalog entry with code '" + code + "'"));
    }

    public void requireVendor(String tenantId, String vendorCode) {
        if (vendorCode == null) {
            return;
        }
        AiqVendor vendor = vendorRepository.findByTenantIdAndVendorCode(tenantId, vendorCode)
                .orElseThrow(() -> new BadRequestException("No vendor with code '" + vendorCode + "'"));
        if (!vendor.getIsActive()) {
            throw new BadRequestException("Vendor '" + vendorCode + "' is not active");
        }
    }

    public AiqAsset requireAsset(String tenantId, Long assetId) {
        if (assetId == null) {
            throw new BadRequestException("asset_id is required");
        }
        return assetRepository.findByTenantIdAndId(tenantId, assetId)
                .orElseThrow(() -> new BadRequestException("No asset with id " + assetId));
    }

    public AiqVisitAssetCapture requireVisitCapture(String tenantId, Long visitCaptureId) {
        return visitAssetCaptureRepository.findByTenantIdAndId(tenantId, visitCaptureId)
                .orElseThrow(() -> new BadRequestException("No visit capture with id " + visitCaptureId));
    }

    public AiqAiVisionResultLog requireAiVisionResult(String tenantId, Long aiVisionResultId) {
        return aiVisionResultLogRepository.findByTenantIdAndId(tenantId, aiVisionResultId)
                .orElseThrow(() -> new BadRequestException("No AI vision result with id " + aiVisionResultId));
    }

    public AiqRoleConfig requireRole(String tenantId, RoleCode roleCode) {
        return roleConfigRepository.findByTenantIdAndRoleCode(tenantId, roleCode)
                .orElseThrow(() -> new BadRequestException("No role config for role_code '" + roleCode + "'"));
    }
}
