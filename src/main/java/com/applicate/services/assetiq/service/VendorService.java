package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.config.TenantContext;
import com.applicate.services.assetiq.dto.vendor.VendorCreateRequest;
import com.applicate.services.assetiq.dto.vendor.VendorResponse;
import com.applicate.services.assetiq.entity.AiqVendor;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.exception.NotFoundException;
import com.applicate.services.assetiq.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Vendor master CRUD — not one of the numbered functional features, but needed as soon as
 * anything (asset registration, AMC) wants to reference a real vendor_code. */
@Service
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public VendorResponse create(VendorCreateRequest request) {
        String tenantId = TenantContext.getTenantId();
        if (vendorRepository.findByTenantIdAndVendorCode(tenantId, request.vendorCode()).isPresent()) {
            throw new BadRequestException("Vendor code '" + request.vendorCode() + "' already exists for this tenant");
        }

        AiqVendor vendor = new AiqVendor();
        vendor.setTenantId(tenantId);
        vendor.setVendorCode(request.vendorCode());
        vendor.setVendorName(request.vendorName());
        vendor.setVendorType(request.vendorType());
        vendor.setGstNumber(request.gstNumber());
        vendor.setContactEmail(request.contactEmail());
        vendor.setContactPhone(request.contactPhone());

        return VendorResponse.from(vendorRepository.save(vendor));
    }

    public VendorResponse get(Long id) {
        return VendorResponse.from(requireOwned(id));
    }

    public List<VendorResponse> listActive() {
        return vendorRepository.findByTenantIdAndIsActiveTrue(TenantContext.getTenantId())
                .stream().map(VendorResponse::from).toList();
    }

    public void deactivate(Long id) {
        AiqVendor vendor = requireOwned(id);
        vendor.setIsActive(false);
        vendorRepository.save(vendor);
    }

    private AiqVendor requireOwned(Long id) {
        return vendorRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new NotFoundException("No vendor with id " + id));
    }
}
