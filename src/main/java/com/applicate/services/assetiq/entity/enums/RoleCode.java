package com.applicate.services.assetiq.entity.enums;

/**
 * Fixed set of field roles the app has capture-eligibility logic for. Stored
 * as VARCHAR via {@code @Enumerated(EnumType.STRING)} — never a DB enum type.
 * Adding a role requires both a new enum constant here and a matching
 * {@code aiq_role_config} row (see {@link com.applicate.services.assetiq.entity.AiqRoleConfig}).
 */
public enum RoleCode {
    SALESMAN,
    SUPERVISOR,
    ASM,
    TECHNICIAN
}
