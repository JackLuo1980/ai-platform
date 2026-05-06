package com.aiplatform.inference.version.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.mapper.InferenceModelMapper;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.mapper.OnlineServiceMapper;
import com.aiplatform.inference.online.service.OnlineServiceService;
import com.aiplatform.inference.evaluation.entity.ModelEvaluation;
import com.aiplatform.inference.evaluation.mapper.ModelEvaluationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final InferenceModelMapper modelMapper;
    private final ModelEvaluationMapper evaluationMapper;
    private final OnlineServiceMapper onlineServiceMapper;
    private final OnlineServiceService onlineServiceService;

    public Map<String, Object> compare(Long modelIdA, Long modelIdB) {
        InferenceModel modelA = modelMapper.selectById(modelIdA);
        InferenceModel modelB = modelMapper.selectById(modelIdB);
        if (modelA == null || modelB == null) {
            throw new RuntimeException("Model not found");
        }

        ModelEvaluation evalA = getLatestEvaluation(modelIdA);
        ModelEvaluation evalB = getLatestEvaluation(modelIdB);

        Map<String, Object> result = new HashMap<>();
        result.put("modelA", buildModelSummary(modelA, evalA));
        result.put("modelB", buildModelSummary(modelB, evalB));
        result.put("comparison", buildComparison(evalA, evalB));
        result.put("recommendation", buildRecommendation(evalA, evalB));

        return result;
    }

    public OnlineService rollback(Long onlineServiceId, Long targetModelId) {
        OnlineService service = onlineServiceMapper.selectById(onlineServiceId);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }

        InferenceModel targetModel = modelMapper.selectById(targetModelId);
        if (targetModel == null) {
            throw new RuntimeException("Target model not found");
        }
        if (!"APPROVED".equals(targetModel.getStatus())) {
            throw new RuntimeException("Target model is not approved");
        }

        k8sRedeploy(service, targetModel);

        service.setModelId(targetModelId);
        service.setModelName(targetModel.getName());
        service.setModelVersion(targetModel.getVersion());
        service.setModelPath(targetModel.getFilePath());
        service.setStatus("RUNNING");
        onlineServiceMapper.updateById(service);

        return service;
    }

    private void k8sRedeploy(OnlineService service, InferenceModel model) {
        try {
            onlineServiceService.stop(service.getId());
        } catch (Exception ignored) {
        }

        onlineServiceService.deploy(
                service.getName(),
                model.getId(),
                service.getReplicas(),
                service.getCpuCores(),
                service.getMemoryMb(),
                service.getReleaseType()
        );
    }

    private ModelEvaluation getLatestEvaluation(Long modelId) {
        return evaluationMapper.selectOne(new LambdaQueryWrapper<ModelEvaluation>()
                .eq(ModelEvaluation::getModelId, modelId)
                .orderByDesc(ModelEvaluation::getCreatedAt)
                .last("LIMIT 1"));
    }

    private Map<String, Object> buildModelSummary(InferenceModel model, ModelEvaluation evaluation) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", model.getId());
        summary.put("name", model.getName());
        summary.put("version", model.getVersion());
        summary.put("framework", model.getFramework());
        summary.put("status", model.getStatus());

        if (evaluation != null) {
            Map<String, Object> metrics = new HashMap<>();
            if (evaluation.getAuc() != null) metrics.put("auc", evaluation.getAuc());
            if (evaluation.getKs() != null) metrics.put("ks", evaluation.getKs());
            if (evaluation.getGini() != null) metrics.put("gini", evaluation.getGini());
            if (evaluation.getAccuracy() != null) metrics.put("accuracy", evaluation.getAccuracy());
            if (evaluation.getF1Score() != null) metrics.put("f1Score", evaluation.getF1Score());
            if (evaluation.getRmse() != null) metrics.put("rmse", evaluation.getRmse());
            if (evaluation.getMae() != null) metrics.put("mae", evaluation.getMae());
            if (evaluation.getR2() != null) metrics.put("r2", evaluation.getR2());
            if (evaluation.getMap() != null) metrics.put("mAP", evaluation.getMap());
            if (evaluation.getIou() != null) metrics.put("IoU", evaluation.getIou());
            summary.put("metrics", metrics);
        }

        return summary;
    }

    private Map<String, Object> buildComparison(ModelEvaluation evalA, ModelEvaluation evalB) {
        Map<String, Object> comparison = new HashMap<>();
        if (evalA == null || evalB == null) {
            comparison.put("note", "One or both models lack evaluation data");
            return comparison;
        }

        List<Map<String, Object>> diffs = new ArrayList<>();
        addDiff(diffs, "auc", evalA.getAuc(), evalB.getAuc(), true);
        addDiff(diffs, "ks", evalA.getKs(), evalB.getKs(), true);
        addDiff(diffs, "gini", evalA.getGini(), evalB.getGini(), true);
        addDiff(diffs, "accuracy", evalA.getAccuracy(), evalB.getAccuracy(), true);
        addDiff(diffs, "f1Score", evalA.getF1Score(), evalB.getF1Score(), true);
        addDiff(diffs, "rmse", evalA.getRmse(), evalB.getRmse(), false);
        addDiff(diffs, "mae", evalA.getMae(), evalB.getMae(), false);
        addDiff(diffs, "r2", evalA.getR2(), evalB.getR2(), true);

        comparison.put("metricDiffs", diffs);
        return comparison;
    }

    private void addDiff(List<Map<String, Object>> diffs, String name, BigDecimal valA, BigDecimal valB, boolean higherIsBetter) {
        if (valA == null || valB == null) return;
        Map<String, Object> diff = new HashMap<>();
        diff.put("metric", name);
        diff.put("valueA", valA);
        diff.put("valueB", valB);
        BigDecimal delta = valB.subtract(valA);
        diff.put("delta", delta);
        diff.put("better", higherIsBetter ? delta.compareTo(BigDecimal.ZERO) > 0 : delta.compareTo(BigDecimal.ZERO) < 0);
        diffs.add(diff);
    }

    private String buildRecommendation(ModelEvaluation evalA, ModelEvaluation evalB) {
        if (evalA == null || evalB == null) return "Insufficient data for recommendation";

        int scoreA = 0, scoreB = 0;
        if (evalA.getAuc() != null && evalB.getAuc() != null) {
            if (evalA.getAuc().compareTo(evalB.getAuc()) > 0) scoreA++; else scoreB++;
        }
        if (evalA.getAccuracy() != null && evalB.getAccuracy() != null) {
            if (evalA.getAccuracy().compareTo(evalB.getAccuracy()) > 0) scoreA++; else scoreB++;
        }
        if (evalA.getF1Score() != null && evalB.getF1Score() != null) {
            if (evalA.getF1Score().compareTo(evalB.getF1Score()) > 0) scoreA++; else scoreB++;
        }

        if (scoreA > scoreB) return "Model A performs better overall";
        if (scoreB > scoreA) return "Model B performs better overall";
        return "Both models perform similarly";
    }
}
