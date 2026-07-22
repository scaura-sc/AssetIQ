package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetCatalogRepository extends JpaRepository<AiqAssetCatalog, Long> {

    Optional<AiqAssetCatalog> findByTenantIdAndId(String tenantId, Long id);

    Optional<AiqAssetCatalog> findByTenantIdAndLevelAndCode(String tenantId, CatalogLevel level, String code);

    List<AiqAssetCatalog> findByTenantIdAndLevel(String tenantId, CatalogLevel level);

    List<AiqAssetCatalog> findByTenantIdAndParentCode(String tenantId, String parentCode);

    List<AiqAssetCatalog> findByTenantIdAndParentCodeAndIsActiveTrue(String tenantId, String parentCode);

    List<AiqAssetCatalog> findByTenantIdAndIsActiveTrue(String tenantId);

    List<AiqAssetCatalog> findByTenantIdAndLevelAndIsActiveTrue(String tenantId, CatalogLevel level);
}
