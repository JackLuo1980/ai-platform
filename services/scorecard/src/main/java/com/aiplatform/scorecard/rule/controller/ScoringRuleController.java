package com.aiplatform.scorecard.rule.controller;

import com.aiplatform.common.model.R;
import com.aiplatform.scorecard.rule.entity.ScoringRule;
import com.aiplatform.scorecard.rule.service.ScoringRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scorecard/rules")
@RequiredArgsConstructor
public class ScoringRuleController {
    private final ScoringRuleService ruleService;


    @PostMapping
    public R<ScoringRule> createRule(@RequestBody ScoringRule rule) {
        return R.ok(ruleService.create(rule));
    }

    @GetMapping("/model/{modelId}")
    public R<List<ScoringRule>> listByModel(@PathVariable Long modelId) {
        return R.ok(ruleService.listByModelId(modelId));
    }

    @PostMapping("/model/{modelId}")
    public R<ScoringRule> create(@PathVariable Long modelId, @RequestBody ScoringRule rule) {
        rule.setModelId(modelId);
        return R.ok(ruleService.create(rule));
    }
}
