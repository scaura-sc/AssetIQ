package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetAssociationRepository extends JpaRepository<AiqAssetAssociation, Long> {

    Optional<AiqAssetAssociation> findByTenantIdAndId(String tenantId, Long id);

    List<AiqAssetAssociation> findByTenantIdAndAssetId(String tenantId, Long assetId);

    Optional<AiqAssetAssociation> findByTenantIdAndAssetIdAndIsActiveTrue(String tenantId, Long assetId);

    List<AiqAssetAssociation> findByTenantIdAndAssetIdInAndIsActiveTrue(String tenantId, List<Long> assetIds);

    List<AiqAssetAssociation> findByTenantIdAndLocationTypeAndLocationCode(String tenantId, LocationType locationType, String locationCode);
}
