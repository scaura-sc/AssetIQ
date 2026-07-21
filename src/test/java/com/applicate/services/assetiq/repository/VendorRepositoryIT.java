package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVendor;
import com.applicate.services.assetiq.entity.enums.VendorType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VendorRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private VendorRepository vendorRepository;

    @Test
    void persistsAndReloadsVendor() {
        AiqVendor vendor = new AiqVendor();
        vendor.setTenantId("tenant-1");
        vendor.setVendorCode("V-001");
        vendor.setVendorName("Acme Refrigeration Services");
        vendor.setVendorType(VendorType.BOTH);
        vendor.setGstNumber("GSTIN123456");
        vendor.setContactEmail("ops@acme.example");
        vendor.setContactPhone("+91-9000000000");

        AiqVendor saved = vendorRepository.saveAndFlush(vendor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<AiqVendor> reloaded = vendorRepository.findByTenantIdAndVendorCode("tenant-1", "V-001");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getVendorType()).isEqualTo(VendorType.BOTH);
    }

    @Test
    void findsByVendorTypeAndActiveFlag() {
        AiqVendor vendor = new AiqVendor();
        vendor.setTenantId("tenant-2");
        vendor.setVendorCode("V-002");
        vendor.setVendorName("ColdChain Supplies");
        vendor.setVendorType(VendorType.SUPPLIER);
        vendorRepository.saveAndFlush(vendor);

        List<AiqVendor> suppliers = vendorRepository.findByTenantIdAndVendorType("tenant-2", VendorType.SUPPLIER);
        assertThat(suppliers).hasSize(1);

        List<AiqVendor> active = vendorRepository.findByTenantIdAndIsActiveTrue("tenant-2");
        assertThat(active).hasSize(1);
    }
}
