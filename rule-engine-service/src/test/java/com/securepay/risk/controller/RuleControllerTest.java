package com.securepay.risk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.risk.dto.CreateRuleRequest;
import com.securepay.risk.dto.UpdateRuleRequest;
import com.securepay.risk.entity.FraudRule;
import com.securepay.risk.service.RuleManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleManagementService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/rules")
    void create_ShouldReturnCreatedRule() throws Exception {

        UUID ruleId = UUID.randomUUID();

        CreateRuleRequest request = new CreateRuleRequest();
        request.setRuleName("High Amount");
        request.setRuleVersion(1);
        request.setRuleExpression("amount > 1000");
        request.setRiskScore(80);
        request.setEnabled(true);

        FraudRule rule = FraudRule.builder()
                .ruleId(ruleId)
                .ruleName("High Amount")
                .ruleVersion(1)
                .ruleExpression("amount > 1000")
                .riskScore(80)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(service.create(any(CreateRuleRequest.class))).thenReturn(rule);

        mockMvc.perform(post("/api/v1/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleId").value(ruleId.toString()))
                .andExpect(jsonPath("$.ruleName").value("High Amount"))
                .andExpect(jsonPath("$.riskScore").value(80));
    }

    @Test
    @DisplayName("GET /api/v1/rules")
    void getAll_ShouldReturnAllRules() throws Exception {

        FraudRule rule = FraudRule.builder()
                .ruleId(UUID.randomUUID())
                .ruleName("Rule")
                .ruleVersion(1)
                .ruleExpression("amount > 100")
                .riskScore(50)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(service.getAll()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/v1/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ruleName").value("Rule"));
    }

    @Test
    @DisplayName("GET /api/v1/rules/{id}")
    void getById_ShouldReturnRule() throws Exception {

        UUID id = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(id)
                .ruleName("Rule")
                .ruleVersion(1)
                .ruleExpression("amount > 100")
                .riskScore(60)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(service.getById(id)).thenReturn(rule);

        mockMvc.perform(get("/api/v1/rules/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleId").value(id.toString()))
                .andExpect(jsonPath("$.ruleName").value("Rule"));
    }

    @Test
    @DisplayName("PUT /api/v1/rules/{id}")
    void update_ShouldReturnUpdatedRule() throws Exception {

        UUID id = UUID.randomUUID();

        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setRuleName("Updated");
        request.setRuleExpression("amount > 500");
        request.setRiskScore(90);
        request.setEnabled(true);

        FraudRule updated = FraudRule.builder()
                .ruleId(id)
                .ruleName("Updated")
                .ruleVersion(1)
                .ruleExpression("amount > 500")
                .riskScore(90)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(service.update(eq(id), any(UpdateRuleRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/rules/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleName").value("Updated"))
                .andExpect(jsonPath("$.riskScore").value(90));
    }

    @Test
    @DisplayName("DELETE /api/v1/rules/{id}")
    void delete_ShouldReturnOk() throws Exception {

        UUID id = UUID.randomUUID();

        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/rules/{id}", id))
                .andExpect(status().isOk());

        verify(service).delete(id);
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/enable")
    void enable_ShouldReturnEnabledRule() throws Exception {

        UUID id = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(id)
                .enabled(true)
                .build();

        when(service.enable(id)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/enable", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/rules/{id}/disable")
    void disable_ShouldReturnDisabledRule() throws Exception {

        UUID id = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(id)
                .enabled(false)
                .build();

        when(service.disable(id)).thenReturn(rule);

        mockMvc.perform(patch("/api/v1/rules/{id}/disable", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }
}