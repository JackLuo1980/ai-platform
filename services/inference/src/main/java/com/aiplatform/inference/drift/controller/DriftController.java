package com.aiplatform.inference.drift.controller;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.common.R;
import com.aiplatform.inference.drift.entity.ModelDriftReport;
import com.aiplatform.inference.drift.service.DriftDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/drift")
@RequiredArgsConstructor
public class DriftController {

    private final DriftDetectionService driftDetectionService;

    @GetMapping("/reports")
    public R<PageResult<ModelDriftReport>> listReports(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String driftType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(driftDetectionService.listReports(modelId, driftType, page, size));
    }

    @PostMapping("/check")
    public R<ModelDriftReport> checkDrift(@RequestBody Map<String, Object> params) {
        Long modelId = Long.valueOf(params.get("modelId").toString());
        String modelName = (String) params.get("modelName");
        String modelVersion = (String) params.getOrDefault("modelVersion", "1.0.0");
        String driftType = (String) params.getOrDefault("driftType", "DATA_DRIFT");

        ModelDriftReport report;
        switch (driftType) {
            case "PREDICTION_DRIFT" -> {
                @SuppressWarnings("unchecked")
                List<Number> baselineList = (List<Number>) params.get("baselineCounts");
                @SuppressWarnings("unchecked")
                List<Number> currentList = (List<Number>) params.get("currentCounts");
                long[] baseline = baselineList.stream().mapToLong(Number::longValue).toArray();
                long[] current = currentList.stream().mapToLong(Number::longValue).toArray();
                report = driftDetectionService.checkPredictionDrift(modelId, modelName, modelVersion, baseline, current);
            }
            case "CONCEPT_DRIFT" -> {
                @SuppressWarnings("unchecked")
                List<Number> accList = (List<Number>) params.get("recentAccuracies");
                double[] accuracies = accList.stream().mapToDouble(Number::doubleValue).toArray();
                double threshold = params.containsKey("thresholdAccuracy")
                        ? ((Number) params.get("thresholdAccuracy")).doubleValue() : 0.85;
                report = driftDetectionService.checkConceptDrift(modelId, modelName, modelVersion, accuracies, threshold);
            }
            default -> {
                @SuppressWarnings("unchecked")
                List<Number> baselineList = (List<Number>) params.get("baseline");
                @SuppressWarnings("unchecked")
                List<Number> currentList = (List<Number>) params.get("current");
                double[] baseline = baselineList.stream().mapToDouble(Number::doubleValue).toArray();
                double[] current = currentList.stream().mapToDouble(Number::doubleValue).toArray();
                report = driftDetectionService.checkDataDrift(modelId, modelName, modelVersion, baseline, current);
            }
        }
        return R.ok(report);
    }

    @GetMapping("/trend/{modelId}")
    public R<Map<String, Object>> getTrend(@PathVariable Long modelId,
                                             @RequestParam(defaultValue = "10") int limit) {
        return R.ok(driftDetectionService.getDriftTrend(modelId, limit));
    }
}
