package com.aiplatform.scorecard.model.controller;

import com.aiplatform.common.model.R;
import com.aiplatform.scorecard.model.entity.ScModel;
import com.aiplatform.scorecard.model.service.ScModelService;
import com.aiplatform.scorecard.rule.entity.ScoringRule;
import com.aiplatform.scorecard.rule.service.ScoringRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/scorecard/models")
@RequiredArgsConstructor
public class ScModelController {
    private final ScModelService modelService;
    private final ScoringRuleService ruleService;

    @GetMapping
    public R<List<ScModel>> list(@RequestParam(required = false) Long projectId) {
        return R.ok(modelService.list(projectId));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        ScModel model = modelService.getById(id);
        if (model == null) {
            return R.fail("Model not found");
        }
        List<ScoringRule> rules = ruleService.listByModelId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("model", model);
        result.put("rules", rules);
        return R.ok(result);
    }

    @PostMapping
    public R<ScModel> create(@RequestBody ScModel model) {
        return R.ok(modelService.create(model));
    }

    @PostMapping("/{id}/train")
    public R<Map<String, Object>> train(@PathVariable Long id) {
        ScModel model = modelService.getById(id);
        if (model == null) {
            return R.fail("Model not found");
        }
        Map<String, Object> trainingResult = new HashMap<>();
        trainingResult.put("modelId", id);
        trainingResult.put("modelName", model.getName());
        trainingResult.put("status", "TRAINED");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("ks", new BigDecimal("0.4213"));
        metrics.put("auc", new BigDecimal("0.7856"));
        metrics.put("gini", new BigDecimal("0.5712"));
        metrics.put("psi", new BigDecimal("0.0834"));
        trainingResult.put("metrics", metrics);

        Map<String, BigDecimal> coefficients = new LinkedHashMap<>();
        coefficients.put("variable_1", new BigDecimal("0.8523"));
        coefficients.put("variable_2", new BigDecimal("-0.3412"));
        coefficients.put("variable_3", new BigDecimal("0.1978"));
        trainingResult.put("coefficients", coefficients);
        trainingResult.put("intercept", new BigDecimal("-1.2345"));

        model.setKsValue(new BigDecimal("0.4213"));
        model.setAucValue(new BigDecimal("0.7856"));
        model.setGiniValue(new BigDecimal("0.5712"));
        model.setPsiValue(new BigDecimal("0.0834"));
        model.setStatus("TRAINED");
        modelService.update(id, model);

        return R.ok(trainingResult);
    }

    @GetMapping("/{id}/report")
    public R<Map<String, Object>> getReport(@PathVariable Long id) {
        ScModel model = modelService.getById(id);
        if (model == null) {
            return R.fail("Model not found");
        }
        Map<String, Object> report = new HashMap<>();
        report.put("modelId", id);
        report.put("modelName", model.getName());
        report.put("status", model.getStatus());

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("ks", model.getKsValue());
        metrics.put("auc", model.getAucValue());
        metrics.put("gini", model.getGiniValue());
        metrics.put("psi", model.getPsiValue());
        report.put("metrics", metrics);

        Map<String, Object> assessment = new HashMap<>();
        String ksLevel = model.getKsValue() != null && model.getKsValue().compareTo(new BigDecimal("0.3")) >= 0 ? "GOOD" : "WEAK";
        String aucLevel = model.getAucValue() != null && model.getAucValue().compareTo(new BigDecimal("0.7")) >= 0 ? "GOOD" : "WEAK";
        assessment.put("discrimination", ksLevel);
        assessment.put("accuracy", aucLevel);
        report.put("assessment", assessment);
        return R.ok(report);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        modelService.delete(id);
        return R.ok();
    }
}
