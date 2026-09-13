package com.enterprise.platform.task.context;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORGANIZATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getOrganizationId() {
        return CURRENT_ORGANIZATION_ID.get();
    }

    public static void setOrganizationId(UUID organizationId) {
        CURRENT_ORGANIZATION_ID.set(organizationId);
    }

    public static UUID getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setUserId(UUID userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static void clear() {
        CURRENT_ORGANIZATION_ID.remove();
        CURRENT_USER_ID.remove();
    }
}
