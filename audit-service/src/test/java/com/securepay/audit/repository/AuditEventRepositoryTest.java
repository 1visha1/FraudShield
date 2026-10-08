package com.fraudshield.audit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AuditEventRepositoryTest {

    @MockitoBean
    private AuditEventRepository repository;

    @Test
    void repositoryLoads() {
    }
}