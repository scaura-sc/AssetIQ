package com.applicate.services.assetiq.entity;

import com.applicate.services.assetiq.entity.enums.VendorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Master: suppliers and/or service vendors, referenced (softly) by aiq_asset and aiq_asset. */
@Getter
@Setter
@Entity
@Table(name = "aiq_vendor")
public class AiqVendor extends AbstractSoftDeletableEntity {

    @Column(name = "vendor_code", nullable = false, length = 30)
    private String vendorCode;

    @Column(name = "vendor_name", nullable = false, length = 100)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false, length = 20)
    private VendorType vendorType;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
}
