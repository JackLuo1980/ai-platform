package com.aiplatform.scorecard.variable.controller;

import com.aiplatform.common.model.R;
import com.aiplatform.scorecard.binning.entity.BinningResult;
import com.aiplatform.scorecard.binning.service.BinningResultService;
import com.aiplatform.scorecard.variable.entity.ScVariable;
import com.aiplatform.scorecard.variable.service.ScVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scorecard/variables")
@RequiredArgsConstructor
public class VariableController {
    private final ScVariableService variableService;
    private final BinningResultService binningService;

    @GetMapping
    public R<List<ScVariable>> list(@RequestParam(required = false) Long projectId) {
        return R.ok(variableService.list(projectId));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        ScVariable variable = variableService.getById(id);
        if (variable == null) {
            return R.fail("Variable not found");
        }
        List<BinningResult> binningResults = binningService.listByVariableId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("variable", variable);
        result.put("binningResults", binningResults);
        return R.ok(result);
    }

    @PostMapping
    public R<ScVariable> create(@RequestBody ScVariable variable) {
        return R.ok(variableService.create(variable));
    }


    @GetMapping("/{id}/analyze")
    public R<Map<String, Object>> getAnalysis(@PathVariable Long id) {
        return analyze(id);
    }

    @PostMapping("/{id}/analyze")
    public R<Map<String, Object>> analyze(@PathVariable Long id) {
        ScVariable variable = variableService.getById(id);
        if (variable == null) {
            return R.fail("Variable not found");
        }
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("variableId", id);
        analysis.put("variableName", variable.getName());
        analysis.put("dtype", variable.getDtype());
        analysis.put("ivValue", variable.getIvValue() != null ? variable.getIvValue() : BigDecimal.ZERO);
        analysis.put("missingRate", variable.getMissingRate() != null ? variable.getMissingRate() : BigDecimal.ZERO);
        analysis.put("suggestion", "IV < 0.02: not predictive; 0.02-0.1: weak; 0.1-0.3: medium; > 0.3: strong");
        return R.ok(analysis);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        variableService.delete(id);
        return R.ok();
    }
}
