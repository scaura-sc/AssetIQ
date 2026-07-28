package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AssetRequestRepository
        extends JpaRepository<AiqAssetRequest, Long>, JpaSpecificationExecutor<AiqAssetRequest> {

    Optional<AiqAssetRequest> findByTenantIdAndId(String tenantId, Long id);
}
