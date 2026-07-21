package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqVendor;
import com.applicate.services.assetiq.entity.enums.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<AiqVendor, Long> {

    Optional<AiqVendor> findByTenantIdAndVendorCode(String tenantId, String vendorCode);

    List<AiqVendor> findByTenantIdAndVendorType(String tenantId, VendorType vendorType);

    List<AiqVendor> findByTenantIdAndIsActiveTrue(String tenantId);
}
