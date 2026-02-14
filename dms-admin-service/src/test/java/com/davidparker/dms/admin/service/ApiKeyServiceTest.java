package com.davidparker.dms.admin.service;

import com.davidparker.dms.admin.model.ApiKey;
import com.davidparker.dms.admin.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private AuditEventClient auditEventClient;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(apiKeyRepository, auditEventClient);
        lenient().doNothing().when(auditEventClient).logEvent(any());
    }

    @Test
    void testCreateApiKey() {
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            ApiKey key = invocation.getArgument(0);
            key.setId(UUID.randomUUID());
            return key;
        });

        Map<String, Object> result = apiKeyService.createApiKey("Test Key", "read,write", null);

        assertNotNull(result);
        assertNotNull(result.get("key"));
        assertTrue(result.get("key").toString().startsWith("dms_"));
        assertEquals("Test Key", result.get("name"));
        assertEquals("read,write", result.get("scopes"));
        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    void testRevokeApiKey() {
        UUID keyId = UUID.randomUUID();
        ApiKey key = ApiKey.builder().id(keyId).name("Test").active(true).build();
        when(apiKeyRepository.findById(keyId)).thenReturn(Optional.of(key));
        when(apiKeyRepository.save(any())).thenReturn(key);

        apiKeyService.revokeApiKey(keyId);

        assertFalse(key.getActive());
        verify(apiKeyRepository).save(key);
    }

    @Test
    void testRevokeApiKeyNotFound() {
        UUID keyId = UUID.randomUUID();
        when(apiKeyRepository.findById(keyId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> apiKeyService.revokeApiKey(keyId));
    }
}
