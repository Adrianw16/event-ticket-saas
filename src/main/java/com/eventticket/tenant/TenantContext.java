package com.eventticket.tenant;

public class TenantContext {

    // ThreadLocal<Long>: each thread gets its own Long slot, isolated.
    private static final ThreadLocal<Long> currentOrgId = new ThreadLocal<>();

    /**
     * Store the current request's org_id in this thread's slot.
     * Called by tenant interceptor PreHandle()
     * @param orgId current user org_id
     */
    public static void setCurrentOrgId(Long orgId) {
        currentOrgId.set(orgId);
    }

    /**
     * Retrieve the org_id from current user.
     * Called by repositories and services that need tenant scoping.
     * @return org_id of current user
     */
    public static Long getCurrentOrgId() {
        Long orgId = currentOrgId.get();
        if (orgId == null) {
            // Fail loudly rather than quietly failing and risking data leaks.
            throw new IllegalStateException(
                    "TenantContext: org_id not set for current thread. " +
                    "This method must only be called within an authenticated request."
            );
        }
        return orgId;
    }

    /**
     * Clears the ThreadLocal value for this thread
     * Called by TenantInterceptor.afterCompletion(), runs even on exception.
     * Without this, thread pool reuse would cause org_id to bleed into unrelated requests.
     */
    public static void clearOrgId() {
        currentOrgId.remove(); // remove() its preferred over set(null), avoids memory leaks in thread pools.
    }


}
