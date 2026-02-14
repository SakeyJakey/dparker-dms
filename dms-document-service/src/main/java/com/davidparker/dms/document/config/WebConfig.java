package com.davidparker.dms.document.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;
import java.util.UUID;

/**
 * Web configuration for request size limits, correlation ID propagation,
 * and security headers.
 */
@Configuration
public class WebConfig {

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
                filterChain.doFilter(request, response);
            }
        };
    }
}
