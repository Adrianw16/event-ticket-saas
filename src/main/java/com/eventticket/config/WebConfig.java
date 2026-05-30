package com.eventticket.config;

import com.eventticket.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig: registers Spring MVC interceptors.
 *
 * Why WebMvcConfigurer?
 * Spring MVC lets you customize the framework by implementing WebMvcConfigurer.
 * addInterceptors() it's the hook for registering HandlerInterceptors globally.
 * Without registering here, Tenant interceptor would exist as a @Component bean
 * but never actually intercept requests.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                // Apply to all API routes that require tenant scoping.
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/login", "/api/v1/auth/register");
    }
}
