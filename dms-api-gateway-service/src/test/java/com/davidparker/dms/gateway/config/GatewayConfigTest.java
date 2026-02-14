package com.davidparker.dms.gateway.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayConfigTest {

    @Test
    void testGatewayConfigExists() {
        GatewayConfig config = new GatewayConfig();
        assertNotNull(config);
    }
}
