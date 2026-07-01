package com.securepay.risk.config;

import com.securepay.risk.entity.FraudRule;
import com.securepay.risk.repository.FraudRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleDataInitializerTest {

    @Mock
    private FraudRuleRepository repository;

    @InjectMocks
    private RuleDataInitializer initializer;

    @Test
    void run_ShouldInsertDefaultRules_WhenRepositoryIsEmpty() throws Exception {

        when(repository.count()).thenReturn(0L);

        when(repository.save(any(FraudRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run();

        ArgumentCaptor<FraudRule> captor =
                ArgumentCaptor.forClass(FraudRule.class);

        verify(repository, times(3)).save(captor.capture());

        List<FraudRule> savedRules = captor.getAllValues();

        assertEquals(3, savedRules.size());

        // Rule 1
        FraudRule highAmount = savedRules.get(0);
        assertEquals("HIGH_AMOUNT", highAmount.getRuleName());
        assertEquals("#amount > 100000", highAmount.getRuleExpression());
        assertEquals(Integer.valueOf(40), highAmount.getRiskScore());
        assertTrue(highAmount.getEnabled());
        assertNotNull(highAmount.getCreatedAt());

        // Rule 2
        FraudRule untrustedDevice = savedRules.get(1);
        assertEquals("UNTRUSTED_DEVICE", untrustedDevice.getRuleName());
        assertEquals("#deviceTrusted == false", untrustedDevice.getRuleExpression());
        assertEquals(Integer.valueOf(60), untrustedDevice.getRiskScore());
        assertTrue(untrustedDevice.getEnabled());
        assertNotNull(untrustedDevice.getCreatedAt());

        // Rule 3
        FraudRule criticalAmount = savedRules.get(2);
        assertEquals("CRITICAL_AMOUNT", criticalAmount.getRuleName());
        assertEquals("#amount > 500000", criticalAmount.getRuleExpression());
        assertEquals(Integer.valueOf(100), criticalAmount.getRiskScore());
        assertTrue(criticalAmount.getEnabled());
        assertNotNull(criticalAmount.getCreatedAt());
    }

    @Test
    void run_ShouldDoNothing_WhenRulesAlreadyExist() throws Exception {

        when(repository.count()).thenReturn(5L);

        initializer.run();

        verify(repository, never()).save(any(FraudRule.class));
    }
}