package com.applicate.services.assetiq.entity.enums;

/**
 * Shared by aiq_asset and aiq_asset_association, though STOCK never appears
 * on an association row (an asset in stock has no active association).
 */
public enum LocationType {
    STOCK,
    OUTLET,
    DISTRIBUTOR,
    WAREHOUSE,
    VEHICLE,
    EMPLOYEE
}
