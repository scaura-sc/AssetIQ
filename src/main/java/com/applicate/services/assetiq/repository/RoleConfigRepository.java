package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleConfigRepository extends JpaRepository<AiqRoleConfig, Long> {

    Optional<AiqRoleConfig> findByTenantIdAndRoleCode(String tenantId, RoleCode roleCode);

    List<AiqRoleConfig> findByTenantId(String tenantId);

    List<AiqRoleConfig> findByTenantIdAndAssetCaptureEligibleTrue(String tenantId);
}
