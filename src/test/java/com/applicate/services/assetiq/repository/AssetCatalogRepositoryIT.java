package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AssetCatalogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AssetCatalogRepository assetCatalogRepository;

    @Test
    void persistsCategoryTypeModelHierarchyAndTraversesByParentCode() {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId("tenant-1");
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("COOLER");
        category.setName("Cooler");
        assetCatalogRepository.saveAndFlush(category);

        AiqAssetCatalog type = new AiqAssetCatalog();
        type.setTenantId("tenant-1");
        type.setLevel(CatalogLevel.TYPE);
        type.setCode("VISI_COOLER");
        type.setName("Visi Cooler");
        type.setParentCode("COOLER");
        assetCatalogRepository.saveAndFlush(type);

        AiqAssetCatalog model = new AiqAssetCatalog();
        model.setTenantId("tenant-1");
        model.setLevel(CatalogLevel.MODEL);
        model.setCode("VC-300L");
        model.setName("VisiCooler 300L");
        model.setParentCode("VISI_COOLER");
        model.setManufacturerName("Acme Refrigeration");
        model.setManufacturerCountry("India");
        model.setDefaultWarrantyMonths((short) 24);
        model.setDefaultUsefulLifeYears((short) 8);
        model.setDefaultDepreciationMethod(DepreciationMethod.SLM);
        model.setDefaultPmFrequencyDays((short) 90);
        model.setDefaultPurityClausePct(new BigDecimal("95.50"));
        model.setCapacity(new BigDecimal("300.00"));
        model.setCapacityUnit("Litres");
        AiqAssetCatalog savedModel = assetCatalogRepository.saveAndFlush(model);

        assertThat(savedModel.getId()).isNotNull();
        assertThat(savedModel.getIsActive()).isTrue();

        List<AiqAssetCatalog> typesUnderCategory = assetCatalogRepository.findByTenantIdAndParentCode("tenant-1", "COOLER");
        assertThat(typesUnderCategory).extracting(AiqAssetCatalog::getCode).containsExactly("VISI_COOLER");

        List<AiqAssetCatalog> modelsUnderType = assetCatalogRepository.findByTenantIdAndParentCode("tenant-1", "VISI_COOLER");
        assertThat(modelsUnderType).extracting(AiqAssetCatalog::getCode).containsExactly("VC-300L");

        Optional<AiqAssetCatalog> reloadedModel =
                assetCatalogRepository.findByTenantIdAndLevelAndCode("tenant-1", CatalogLevel.MODEL, "VC-300L");
        assertThat(reloadedModel).isPresent();
        assertThat(reloadedModel.get().getDefaultDepreciationMethod()).isEqualTo(DepreciationMethod.SLM);
        assertThat(reloadedModel.get().getDefaultPurityClausePct()).isEqualByComparingTo("95.50");
    }

    @Test
    void findsByLevelAndFiltersByActiveFlag() {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId("tenant-2");
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("FRIDGE");
        category.setName("Fridge");
        assetCatalogRepository.saveAndFlush(category);

        List<AiqAssetCatalog> categories = assetCatalogRepository.findByTenantIdAndLevel("tenant-2", CatalogLevel.CATEGORY);
        assertThat(categories).hasSize(1);

        List<AiqAssetCatalog> active = assetCatalogRepository.findByTenantIdAndIsActiveTrue("tenant-2");
        assertThat(active).hasSize(1);
    }
}
