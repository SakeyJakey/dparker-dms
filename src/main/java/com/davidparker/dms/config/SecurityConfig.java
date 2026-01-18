package com.davidparker.dms.config;

import com.davidparker.dms.security.ApplicationIsolationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final ApplicationIsolationFilter applicationIsolationFilter;

    public SecurityConfig(ApplicationIsolationFilter applicationIsolationFilter) {
        this.applicationIsolationFilter = applicationIsolationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(aadJwtConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers("/api/v1/llm/**").hasRole("DMS.LLM.Service")
                .requestMatchers("/api/v1/admin/**").hasRole("DMS.Admin")
                .requestMatchers("/api/v1/documents/**").hasAnyRole("DMS.davidparker-lv-bmth", "DMS.User")
                .anyRequest().authenticated()
            )
            .addFilterBefore(applicationIsolationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    public org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, org.springframework.security.core.Authentication> aadJwtConverter() {
        return new com.davidparker.dms.security.AadJwtAuthenticationConverter();
    }
}
