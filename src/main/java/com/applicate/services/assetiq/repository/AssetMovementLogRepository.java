package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AssetMovementLogRepository
        extends JpaRepository<AiqAssetMovementLog, Long>, JpaSpecificationExecutor<AiqAssetMovementLog> {

    List<AiqAssetMovementLog> findByTenantIdAndAssetIdOrderByMovedAtDesc(String tenantId, Long assetId);

    List<AiqAssetMovementLog> findByTenantIdAndMovementType(String tenantId, MovementType movementType);
}
