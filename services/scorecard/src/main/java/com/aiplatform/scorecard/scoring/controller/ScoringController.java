package com.aiplatform.scorecard.scoring.controller;

import com.aiplatform.common.model.R;
import com.aiplatform.scorecard.rule.entity.ScoringRule;
import com.aiplatform.scorecard.rule.service.ScoringRuleService;
import com.aiplatform.scorecard.scoring.entity.ScoringResult;
import com.aiplatform.scorecard.scoring.service.ScoringResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/scorecard/scoring")
@RequiredArgsConstructor
public class ScoringController {
    private final ScoringResultService scoringResultService;
    private final ScoringRuleService ruleService;
    private final ObjectMapper objectMapper;

    @PostMapping("/{modelId}")
    public R<Map<String, Object>> score(@PathVariable Long modelId, @RequestBody Map<String, Object> input) {
        try {
            List<ScoringRule> rules = ruleService.listByModelId(modelId);
            if (rules.isEmpty()) {
                return R.fail("No scoring rules found for model " + modelId);
            }

            ScoringRule rule = rules.get(0);
            BigDecimal baseScore = rule.getBaseScore() != null ? rule.getBaseScore() : new BigDecimal("600");
            BigDecimal pdo = rule.getPdo() != null ? rule.getPdo() : new BigDecimal("20");

            BigDecimal totalScore = baseScore;
            Map<String, Object> scoreDetails = new LinkedHashMap<>();
            scoreDetails.put("baseScore", baseScore);
            scoreDetails.put("pdo", pdo);

            List<Map<String, Object>> variableScores = new ArrayList<>();
            for (Map.Entry<String, Object> entry : input.entrySet()) {
                Map<String, Object> varScore = new HashMap<>();
                varScore.put("variable", entry.getKey());
                varScore.put("inputValue", entry.getValue());
                BigDecimal pts = new BigDecimal("15");
                varScore.put("points", pts);
                variableScores.add(varScore);
                totalScore = totalScore.add(pts);
            }
            scoreDetails.put("variableScores", variableScores);
            scoreDetails.put("totalScore", totalScore);

            ScoringResult result = new ScoringResult();
            result.setModelId(modelId);
            result.setScore(totalScore);
            result.setInputJson(objectMapper.writeValueAsString(input));
            scoringResultService.create(result);

            Map<String, Object> response = new HashMap<>();
            response.put("score", totalScore);
            response.put("modelId", modelId);
            response.put("details", scoreDetails);
            return R.ok(response);
        } catch (Exception e) {
            return R.fail("Scoring failed: " + e.getMessage());
        }
    }

    @GetMapping("/{modelId}/results")
    public R<List<ScoringResult>> getResults(@PathVariable Long modelId) {
        return R.ok(scoringResultService.listByModelId(modelId));
    }
}
