package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RoleConfigRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private RoleConfigRepository roleConfigRepository;

    @Test
    void persistsAndReloadsRoleConfigWithGeneratedIdAndAuditColumns() {
        AiqRoleConfig config = new AiqRoleConfig();
        config.setTenantId("tenant-1");
        config.setRoleCode(RoleCode.TECHNICIAN);
        config.setRoleName("Technician");
        config.setAssetCaptureEligible(true);
        config.setDescription("Field service technician");

        AiqRoleConfig saved = roleConfigRepository.saveAndFlush(config);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<AiqRoleConfig> reloaded = roleConfigRepository.findById(saved.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getRoleCode()).isEqualTo(RoleCode.TECHNICIAN);
        assertThat(reloaded.get().getTenantId()).isEqualTo("tenant-1");
        assertThat(reloaded.get().getAssetCaptureEligible()).isTrue();
    }

    @Test
    void findsByTenantAndRoleCode() {
        AiqRoleConfig config = new AiqRoleConfig();
        config.setTenantId("tenant-2");
        config.setRoleCode(RoleCode.SALESMAN);
        config.setRoleName("Salesman");
        config.setAssetCaptureEligible(true);
        roleConfigRepository.saveAndFlush(config);

        Optional<AiqRoleConfig> found = roleConfigRepository.findByTenantIdAndRoleCode("tenant-2", RoleCode.SALESMAN);
        assertThat(found).isPresent();

        List<AiqRoleConfig> eligible = roleConfigRepository.findByTenantIdAndAssetCaptureEligibleTrue("tenant-2");
        assertThat(eligible).hasSize(1);
    }

    @Test
    void assetCaptureEligibleDefaultsToFalseWhenNotSet() {
        AiqRoleConfig config = new AiqRoleConfig();
        config.setTenantId("tenant-3");
        config.setRoleCode(RoleCode.SUPERVISOR);
        config.setRoleName("Supervisor");

        AiqRoleConfig saved = roleConfigRepository.saveAndFlush(config);

        assertThat(saved.getAssetCaptureEligible()).isFalse();
    }
}
