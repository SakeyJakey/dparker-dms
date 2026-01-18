package com.davidparker.dms.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.ai.openai.endpoint:}")
    private String openAiEndpoint;

    @Value("${azure.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${azure.ai.search.endpoint:}")
    private String searchEndpoint;

    @Value("${azure.ai.search.api-key:}")
    private String searchApiKey;

    @Bean
    public OpenAIClient openAIClient(TokenCredential tokenCredential) {
        OpenAIClientBuilder builder = new OpenAIClientBuilder();
        if (openAiEndpoint != null && !openAiEndpoint.isEmpty()) {
            builder.endpoint(openAiEndpoint);
        }
        if (openAiApiKey != null && !openAiApiKey.isEmpty()) {
            builder.credential(new com.azure.core.credential.AzureKeyCredential(openAiApiKey));
        } else {
            builder.credential(tokenCredential);
        }
        return builder.buildClient();
    }

    @Bean
    public SearchClient searchClient() {
        if (searchEndpoint == null || searchEndpoint.isEmpty() || 
            searchApiKey == null || searchApiKey.isEmpty()) {
            return null; // Return null if not configured
        }
        return new SearchClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new com.azure.core.credential.AzureKeyCredential(searchApiKey))
            .indexName("documents-index")
            .buildClient();
    }
}
