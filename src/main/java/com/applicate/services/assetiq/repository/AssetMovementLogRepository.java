package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetMovementLogRepository extends JpaRepository<AiqAssetMovementLog, Long> {

    List<AiqAssetMovementLog> findByTenantIdAndAssetIdOrderByMovedAtDesc(String tenantId, Long assetId);

    List<AiqAssetMovementLog> findByTenantIdAndMovementType(String tenantId, MovementType movementType);
}
