package com.applicate.services.assetiq.config;

/**
 * Request-scoped current tenant, populated by {@link TenantFilter} from the
 * {@code X-Tenant-Id} header. Every service method reads the tenant from
 * here rather than accepting it as a parameter threaded down from the
 * controller.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static String getTenantId() {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant bound to the current request");
        }
        return tenantId;
    }

    static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    static void clear() {
        CURRENT_TENANT.remove();
    }
}
