package com.aiplatform.lab.experiment;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lab/experiments")
@RequiredArgsConstructor
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping
    public R<PageResult<Experiment>> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) Long tenantId,
                                          @RequestParam(required = false) Long projectId) {
        return R.ok(experimentService.list(tenantId, projectId, page, size));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getDetail(@PathVariable Long id) {
        return R.ok(experimentService.getDetail(id));
    }

    @GetMapping("/{id}/metrics")
    public R<Map<String, Object>> getMetrics(@PathVariable Long id) {
        return R.ok(experimentService.getMetrics(id));
    }

    @PostMapping
    public R<Experiment> create(@RequestBody Experiment experiment) {
        return R.ok(experimentService.create(experiment));
    }

    @PostMapping("/{id}/runs")
    public R<String> createRun(@PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        String runName = (String) body.getOrDefault("runName", "run-" + System.currentTimeMillis());
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) body.get("params");
        @SuppressWarnings("unchecked")
        Map<String, Double> metrics = (Map<String, Double>) body.get("metrics");
        return R.ok(experimentService.createRun(id, runName, params, metrics));
    }
}
