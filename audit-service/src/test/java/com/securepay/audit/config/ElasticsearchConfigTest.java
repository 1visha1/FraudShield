package com.fraudshield.audit.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ElasticsearchConfigTest {

    @Test
    void testClientConfiguration() {

        ElasticsearchConfig config = new ElasticsearchConfig();

        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 9200);

        ClientConfiguration clientConfiguration = config.clientConfiguration();

        assertNotNull(clientConfiguration);
    }

    @Test
    void testClientConfigurationWithCustomHostAndPort() {

        ElasticsearchConfig config = new ElasticsearchConfig();

        ReflectionTestUtils.setField(config, "host", "elastic-server");
        ReflectionTestUtils.setField(config, "port", 9300);

        ClientConfiguration clientConfiguration = config.clientConfiguration();

        assertNotNull(clientConfiguration);
    }
}