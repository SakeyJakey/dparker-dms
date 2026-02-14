package com.davidparker.dms.admin.config;

import com.davidparker.dms.admin.security.AadJwtAuthenticationConverter;
import com.davidparker.dms.admin.security.ApplicationIsolationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired(required = false)
    private ApplicationIsolationFilter applicationIsolationFilter;

    @Autowired(required = false)
    private CorsFilter corsFilter;

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isDevMode = isDevMode();
        
        if (isDevMode) {
            return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
        }
        
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(aadJwtConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("DMS.Admin")
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());

        if (corsFilter != null) {
            http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        }
        if (applicationIsolationFilter != null) {
            http.addFilterBefore(applicationIsolationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        return http.build();
    }

    private boolean isDevMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "docker".equals(profile) || "test".equals(profile)) {
                return true;
            }
        }
        return activeProfiles.length == 0;
    }

    @Bean
    public org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, org.springframework.security.authentication.AbstractAuthenticationToken> aadJwtConverter() {
        return new AadJwtAuthenticationConverter();
    }
}
