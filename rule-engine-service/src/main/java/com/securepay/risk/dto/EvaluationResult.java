package com.securepay.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EvaluationResult {

    private Integer score;

    private List<String> matchedRules;
}