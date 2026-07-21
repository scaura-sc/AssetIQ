package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAsset;
import com.applicate.services.assetiq.entity.enums.AssetStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AssetRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AssetRepository assetRepository;

    private AiqAsset newAsset(String tenantId, String assetNumber, String serialNumber) {
        AiqAsset asset = new AiqAsset();
        asset.setTenantId(tenantId);
        asset.setAssetNumber(assetNumber);
        asset.setSerialNumber(serialNumber);
        asset.setAssetName("VisiCooler 300L");
        asset.setCategoryCode("COOLER");
        asset.setTypeCode("VISI_COOLER");
        asset.setModelCode("VC-300L");
        asset.setPurchaseDate(LocalDate.of(2025, 1, 15));
        asset.setPurchasePrice(new BigDecimal("45000.00"));
        asset.setCreatedBy("system");
        asset.setUpdatedBy("system");
        return asset;
    }

    @Test
    void persistsAssetWithDefaultStatusAndAuditColumns() {
        AiqAsset asset = newAsset("tenant-1", "AST-0001", "SN-0001");

        AiqAsset saved = assetRepository.saveAndFlush(asset);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAssetStatus()).isEqualTo(AssetStatus.STOCK);
        assertThat(saved.getAhsStaleFlag()).isFalse();
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<AiqAsset> reloaded = assetRepository.findByTenantIdAndAssetNumber("tenant-1", "AST-0001");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getSerialNumber()).isEqualTo("SN-0001");
    }

    @Test
    void findsBySerialWithinCategoryAndByLocation() {
        AiqAsset asset = newAsset("tenant-2", "AST-0002", "SN-0002");
        asset.setLocationType(LocationType.OUTLET);
        asset.setLocationCode("OUT-100");
        assetRepository.saveAndFlush(asset);

        Optional<AiqAsset> bySerial =
                assetRepository.findByTenantIdAndCategoryCodeAndSerialNumber("tenant-2", "COOLER", "SN-0002");
        assertThat(bySerial).isPresent();

        List<AiqAsset> atLocation =
                assetRepository.findByTenantIdAndLocationTypeAndLocationCode("tenant-2", LocationType.OUTLET, "OUT-100");
        assertThat(atLocation).hasSize(1);
    }

    @Test
    void findsByAssetStatus() {
        AiqAsset asset = newAsset("tenant-3", "AST-0003", "SN-0003");
        assetRepository.saveAndFlush(asset);

        List<AiqAsset> inStock = assetRepository.findByTenantIdAndAssetStatus("tenant-3", AssetStatus.STOCK);
        assertThat(inStock).hasSize(1);
    }
}
