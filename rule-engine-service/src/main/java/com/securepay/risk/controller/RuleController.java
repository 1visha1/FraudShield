package com.securepay.risk.controller;

import com.securepay.risk.dto.CreateRuleRequest;
import com.securepay.risk.dto.UpdateRuleRequest;
import com.securepay.risk.entity.FraudRule;
import com.securepay.risk.service.RuleManagementService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleManagementService service;

    @PostMapping
    public FraudRule create(
            @RequestBody
            CreateRuleRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<FraudRule> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public FraudRule getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public FraudRule update(
            @PathVariable UUID id,
            @RequestBody
            UpdateRuleRequest request) {

        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }

    @PatchMapping("/{id}/enable")
    public FraudRule enable(
            @PathVariable UUID id) {

        return service.enable(id);
    }

    @PatchMapping("/{id}/disable")
    public FraudRule disable(
            @PathVariable UUID id) {

        return service.disable(id);
    }
}