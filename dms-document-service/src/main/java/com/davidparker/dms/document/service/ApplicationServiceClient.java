package com.davidparker.dms.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationServiceClient {

    private final WebClient webClient;

    public ApplicationServiceClient(@Value("${dms.services.admin-service-url}") String adminServiceUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(adminServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public Mono<Map<String, Object>> getApplication(UUID applicationId) {
        return webClient.get()
            .uri("/api/v1/admin/applications/{id}", applicationId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .onErrorResume(e -> {
                System.err.println("Failed to get application: " + e.getMessage());
                return Mono.empty();
            });
    }

    public Mono<Map<String, Object>> getApplicationByEntraId(String entraAppId) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/admin/applications")
                .queryParam("entraAppId", entraAppId)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .onErrorResume(e -> {
                System.err.println("Failed to get application by Entra ID: " + e.getMessage());
                return Mono.empty();
            });
    }
}
