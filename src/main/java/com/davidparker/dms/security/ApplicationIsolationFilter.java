package com.davidparker.dms.security;

import com.davidparker.dms.model.RegisteredApplication;
import com.davidparker.dms.repository.RegisteredApplicationRepository;
import com.davidparker.dms.service.ApplicationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApplicationIsolationFilter extends OncePerRequestFilter {

    private final RegisteredApplicationRepository applicationRepository;

    public ApplicationIsolationFilter(RegisteredApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String appId = jwtAuth.getToken().getClaimAsString("azp");

            if (appId != null) {
                RegisteredApplication app = applicationRepository.findByEntraAppId(appId)
                    .orElse(null);

                if (app != null) {
                    // Set application context for the request
                    ApplicationContext.setCurrent(app);

                    try {
                        filterChain.doFilter(request, response);
                    } finally {
                        ApplicationContext.clear();
                    }
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
