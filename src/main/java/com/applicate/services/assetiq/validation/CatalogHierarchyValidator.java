package com.applicate.services.assetiq.validation;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import org.springframework.stereotype.Component;

/**
 * Enforces the CATEGORY -&gt; TYPE -&gt; MODEL shape of aiq_asset_catalog.
 * All violations are client input errors (400) — the caller supplied a
 * level/parent_code combination, or a category/type/model triple, that
 * doesn't form a valid chain.
 */
@Component
public class CatalogHierarchyValidator {

    private final AssetCatalogRepository assetCatalogRepository;

    public CatalogHierarchyValidator(AssetCatalogRepository assetCatalogRepository) {
        this.assetCatalogRepository = assetCatalogRepository;
    }

    /** Validates a row being created/updated at the given level has a parent_code appropriate for that level. */
    public void validateForCreate(String tenantId, CatalogLevel level, String parentCode) {
        switch (level) {
            case CATEGORY -> {
                if (parentCode != null) {
                    throw new BadRequestException("A CATEGORY row must not have a parent_code");
                }
            }
            case TYPE -> {
                if (parentCode == null) {
                    throw new BadRequestException("A TYPE row requires parent_code (a CATEGORY code)");
                }
                requireActiveAt(tenantId, CatalogLevel.CATEGORY, parentCode);
            }
            case MODEL -> {
                if (parentCode == null) {
                    throw new BadRequestException("A MODEL row requires parent_code (a TYPE code)");
                }
                requireActiveAt(tenantId, CatalogLevel.TYPE, parentCode);
            }
        }
    }

    /**
     * Validates that category_code/type_code/model_code (as used on aiq_asset)
     * form one coherent chain, not just three independently-existing codes.
     */
    public void validateAssetCatalogChain(String tenantId, String categoryCode, String typeCode, String modelCode) {
        requireActiveAt(tenantId, CatalogLevel.CATEGORY, categoryCode);
        AiqAssetCatalog type = requireActiveAt(tenantId, CatalogLevel.TYPE, typeCode);
        AiqAssetCatalog model = requireActiveAt(tenantId, CatalogLevel.MODEL, modelCode);

        if (!categoryCode.equals(type.getParentCode())) {
            throw new BadRequestException(
                    "type_code '" + typeCode + "' does not belong to category_code '" + categoryCode + "'");
        }
        if (!typeCode.equals(model.getParentCode())) {
            throw new BadRequestException(
                    "model_code '" + modelCode + "' does not belong to type_code '" + typeCode + "'");
        }
    }

    /** Same chain check as validateAssetCatalogChain, but for callers that only know category+type
     * (no model) — e.g. an asset request naming just "a COOLER of type VISI_COOLER". */
    public void validateCategoryTypeChain(String tenantId, String categoryCode, String typeCode) {
        requireActiveAt(tenantId, CatalogLevel.CATEGORY, categoryCode);
        AiqAssetCatalog type = requireActiveAt(tenantId, CatalogLevel.TYPE, typeCode);

        if (!categoryCode.equals(type.getParentCode())) {
            throw new BadRequestException(
                    "type_code '" + typeCode + "' does not belong to category_code '" + categoryCode + "'");
        }
    }

    private AiqAssetCatalog requireActiveAt(String tenantId, CatalogLevel level, String code) {
        AiqAssetCatalog entry = assetCatalogRepository.findByTenantIdAndLevelAndCode(tenantId, level, code)
                .orElseThrow(() -> new BadRequestException("No " + level + " catalog entry with code '" + code + "'"));
        if (!entry.getIsActive()) {
            throw new BadRequestException(level + " catalog entry '" + code + "' is not active");
        }
        return entry;
    }
}
