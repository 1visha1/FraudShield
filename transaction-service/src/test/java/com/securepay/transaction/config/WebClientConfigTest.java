package com.fraudshield.transaction.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class WebClientConfigTest {

    private WebClientConfig webClientConfig;

    @BeforeEach
    void setUp() {
        webClientConfig = new WebClientConfig();
    }

    @Test
    void webClientBuilder_ShouldReturnBuilder_WhenSslDisabled() throws Exception {

        ReflectionTestUtils.setField(webClientConfig, "sslEnabled", false);

        WebClient.Builder builder = webClientConfig.webClientBuilder();

        assertNotNull(builder);
        assertTrue(builder instanceof WebClient.Builder);
    }
}