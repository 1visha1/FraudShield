package com.fraudshield.audit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AuditServiceApplicationTests {

	@Test
	void contextLoads() {
		// Context starts successfully if this test passes
	}

	@Test
	void applicationClass_ShouldBeInstantiable() {
		AuditServiceApplication application = new AuditServiceApplication();

		assertNotNull(application);
	}
}