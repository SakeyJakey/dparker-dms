package com.davidparker.dms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Document Management System API")
                .version("1.0.0")
                .description("Enterprise DMS with RBAC, multi-application isolation, LLM query support, " +
                    "and PCI-DSS/ISO 27001/GDPR compliance"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Azure AD JWT Token")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
