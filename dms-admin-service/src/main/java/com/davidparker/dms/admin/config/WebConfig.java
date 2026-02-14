package com.davidparker.dms.admin.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Web configuration for request size limits, correlation ID propagation,
 * and security headers.
 */
@Configuration
public class WebConfig {

    /**
     * Filter that propagates or generates correlation IDs for request tracing.
     */
    @Bean
    public OncePerRequestFilter correlationIdFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null || correlationId.isBlank()) {
                    correlationId = UUID.randomUUID().toString();
                }
                response.setHeader("X-Correlation-ID", correlationId);
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Cache-Control", "no-store");
                response.setHeader("Content-Security-Policy", "default-src 'self'");
                filterChain.doFilter(request, response);
            }
        };
    }
}
