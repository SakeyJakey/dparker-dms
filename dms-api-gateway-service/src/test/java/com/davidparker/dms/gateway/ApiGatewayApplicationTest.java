package com.davidparker.dms.gateway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGatewayApplicationTest {

    @Test
    void testApplicationClassExists() {
        ApiGatewayApplication app = new ApiGatewayApplication();
        assertNotNull(app);
    }
}
