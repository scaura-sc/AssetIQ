package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<AiqAsset, Long> {

    Optional<AiqAsset> findByTenantIdAndId(String tenantId, Long id);

    List<AiqAsset> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<AiqAsset> findByTenantIdAndAssetNumber(String tenantId, String assetNumber);

    Optional<AiqAsset> findByTenantIdAndCategoryCodeAndSerialNumber(String tenantId, String categoryCode, String serialNumber);

    List<AiqAsset> findByTenantIdAndLocationTypeAndLocationCode(String tenantId, LocationType locationType, String locationCode);

    List<AiqAsset> findByTenantIdAndAssetStatus(String tenantId, AssetStatus assetStatus);

    List<AiqAsset> findByTenantIdAndTerritoryCode(String tenantId, String territoryCode);
}
