package io.github.expertflow.internal;

/**
 * Thread-local holder used internally by the library to mark current tenant.
 * The library's repository/transaction wiring will set and clear this value around operations.
 */
public final class TenantContext {
    private static final ThreadLocal<String> current = new ThreadLocal<>();


    private TenantContext() {}

    public static void setCurrentTenant(String tenantId) {
        current.set(tenantId);
    }

    public static String getCurrentTenant() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }
}
