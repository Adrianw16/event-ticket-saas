package com.eventticket.security;

import com.eventticket.service.JwtService;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Http2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    @Autowired
    private JwtService jwtService;

    /**
     * OncePerRequestFilter: runs exactly once per request.
     * Extracts JWT from "Authorization: Bearer <token>" header
     * Validates content and populates SecurityContext.
     * if token is missing/expired/invalid , next filter in chain runs (Spring Security handles the 401)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException{

        //Extract "Authorization" header
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            // Extract token, remove "Bearer" prefix
            String token = authHeader.substring(7);

            try{
                //Validate Header
                //Throws JwtException if invalid/expired
                Long userId = jwtService.extractUserId(token);
                Long orgId = jwtService.extractOrgId(token);

                // Create authentication token (no credential/authorities yet)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, null);

                //Store org_id in auth details for later use (tenant context)
                auth.setDetails(orgId);

                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);
            }catch (JwtException e){
                // Token invalid or expired.
                // Intentionally DO NOT set authentication.
                // Spring Security will return 401 when accessing protected endpoints.
                log.warn("JWT validation failed: {}", e.getMessage());
            }
        }

        //Continue the filter chain
        filterChain.doFilter(request, response);

    }

}
