package com.aiplatform.inference.evaluation.controller;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.common.R;
import com.aiplatform.inference.evaluation.entity.ModelEvaluation;
import com.aiplatform.inference.evaluation.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping
    public R<PageResult<ModelEvaluation>> listEvaluations(
            @RequestParam(required = false) Long modelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(evaluationService.listEvaluations(modelId, page, size));
    }

    @PostMapping
    public R<ModelEvaluation> evaluate(@RequestBody Map<String, Object> params) {
        Long modelId = Long.valueOf(params.get("modelId").toString());
        String modelName = (String) params.get("modelName");
        String modelVersion = (String) params.getOrDefault("modelVersion", "1.0.0");
        String template = (String) params.getOrDefault("template", "binary_classification");

        ModelEvaluation result;
        switch (template) {
            case "multi_class" -> {
                @SuppressWarnings("unchecked")
                List<Number> actualList = (List<Number>) params.get("actualLabels");
                @SuppressWarnings("unchecked")
                List<Number> predictedList = (List<Number>) params.get("predictedLabels");
                int numClasses = params.containsKey("numClasses")
                        ? Integer.valueOf(params.get("numClasses").toString()) : 2;
                int[] actual = actualList.stream().mapToInt(Number::intValue).toArray();
                int[] predicted = predictedList.stream().mapToInt(Number::intValue).toArray();
                result = evaluationService.evaluateMultiClass(modelId, modelName, modelVersion, actual, predicted, numClasses);
            }
            case "regression" -> {
                @SuppressWarnings("unchecked")
                List<Number> actualList = (List<Number>) params.get("actual");
                @SuppressWarnings("unchecked")
                List<Number> predictedList = (List<Number>) params.get("predicted");
                double[] actual = actualList.stream().mapToDouble(Number::doubleValue).toArray();
                double[] predicted = predictedList.stream().mapToDouble(Number::doubleValue).toArray();
                result = evaluationService.evaluateRegression(modelId, modelName, modelVersion, actual, predicted);
            }
            case "detection" -> {
                double map = params.containsKey("mAP") ? ((Number) params.get("mAP")).doubleValue() : 0;
                double iou = params.containsKey("IoU") ? ((Number) params.get("IoU")).doubleValue() : 0;
                result = evaluationService.evaluateDetection(modelId, modelName, modelVersion, map, iou);
            }
            case "custom" -> {
                String scriptPath = (String) params.get("scriptPath");
                @SuppressWarnings("unchecked")
                Map<String, Object> evalParams = (Map<String, Object>) params.getOrDefault("params", Map.of());
                result = evaluationService.evaluateCustom(modelId, modelName, modelVersion, scriptPath, evalParams);
            }
            default -> {
                @SuppressWarnings("unchecked")
                List<Number> actualList = (List<Number>) params.get("actualLabels");
                @SuppressWarnings("unchecked")
                List<Number> scoreList = (List<Number>) params.get("predictedScores");
                double threshold = params.containsKey("threshold")
                        ? ((Number) params.get("threshold")).doubleValue() : 0.5;
                double[] actual = actualList.stream().mapToDouble(Number::doubleValue).toArray();
                double[] scores = scoreList.stream().mapToDouble(Number::doubleValue).toArray();
                result = evaluationService.evaluateBinaryClassification(modelId, modelName, modelVersion, actual, scores, threshold);
            }
        }
        return R.ok(result);
    }

    @GetMapping("/templates")
    public R<List<Map<String, Object>>> getTemplates() {
        return R.ok(evaluationService.getTemplates());
    }

    @GetMapping("/{id}/report")
    public R<ModelEvaluation> getReport(@PathVariable Long id) {
        return R.ok(evaluationService.getReport(id));
    }
}
