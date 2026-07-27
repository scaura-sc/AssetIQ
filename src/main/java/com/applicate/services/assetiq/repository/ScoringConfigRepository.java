package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqScoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoringConfigRepository extends JpaRepository<AiqScoringConfig, Long> {

    Optional<AiqScoringConfig> findByTenantId(String tenantId);
}
