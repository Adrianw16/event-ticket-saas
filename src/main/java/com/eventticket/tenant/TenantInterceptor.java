package com.eventticket.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    /**
     * Runs BEFORE the controller method.
     * Extracts org_id from SecurityContext (populated by JwtAuthenticationFilter)
     * and stores it in TenantContext.
     * @param request current HTTP request.
     * @param response current HTTP response.
     * @param handler chosen handler to execute, for type and/or instance evaluation.
     * @return true -> continue processing (normal path for authenticated requests).
     *         false -> abort (should not happen here, Spring Security already handles 401).
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Only set TenantContext for authenticated requests.
        // Unauthenticated request coming from public endpoints are skipped.
        if (auth != null && auth.isAuthenticated() && auth.getDetails() instanceof Long) {
            Long orgId = (Long) auth.getDetails();
            TenantContext.setCurrentOrgId(orgId);
            log.debug("TenantContext set: org_id={} for {}", orgId, request.getRequestURI());
        }
        return true;
    }

    /**
     * Runs AFTER the response is fully commited, even if there was an exception.
     * Why afterCompletion and not postHandle?
     * postHandle does NOT run if the controller method throws an exception.
     * afterCompletion always runs. Without clearing here, thread-pool reuse
     * would cause subsequent requests on the same thread to inherit a stale org_id.
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param ex any exception thrown on handler execution, if any; this does not
     * include exceptions that have been handled through an exception resolver
     */
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex){
        TenantContext.clearOrgId(); //remove() under the hood, avoids ThreadLocal memory leaks.
        log.debug("TenantContext cleared for {}", request.getRequestURI());
    }
}
