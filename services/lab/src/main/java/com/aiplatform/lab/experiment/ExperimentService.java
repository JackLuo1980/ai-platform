package com.aiplatform.lab.experiment;

import com.aiplatform.lab.common.MlflowClient;
import com.aiplatform.lab.common.PageResult;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentMapper experimentMapper;
    private final MlflowClient mlflowClient;

    public Experiment create(Experiment experiment) {
        String mlflowId = mlflowClient.createExperiment(experiment.getName());
        experiment.setMlflowExperimentId(mlflowId);
        experimentMapper.insert(experiment);
        return experiment;
    }

    public Experiment getById(Long id) {
        return experimentMapper.selectById(id);
    }

    public PageResult<Experiment> list(Long tenantId, Long projectId, int page, int size) {
        LambdaQueryWrapper<Experiment> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Experiment::getTenantId, tenantId);
        if (projectId != null) wrapper.eq(Experiment::getProjectId, projectId);
        wrapper.orderByDesc(Experiment::getCreatedAt);
        IPage<Experiment> result = experimentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Map<String, Object> getDetail(Long id) {
        Experiment exp = experimentMapper.selectById(id);
        if (exp == null) return null;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("experiment", exp);

        if (exp.getMlflowExperimentId() != null) {
            try {
                JSONArray runs = mlflowClient.searchRuns(exp.getMlflowExperimentId(), null,
                        "attribute.start_time DESC", 100);
                detail.put("runs", runs);
            } catch (Exception e) {
                detail.put("runs", Collections.emptyList());
                detail.put("mlflowError", e.getMessage());
            }
        }

        return detail;
    }

    public Map<String, Object> getMetrics(Long id) {
        Experiment exp = experimentMapper.selectById(id);
        if (exp == null) return null;

        Map<String, Object> metrics = new LinkedHashMap<>();
        if (exp.getMlflowExperimentId() != null) {
            try {
                JSONArray runs = mlflowClient.searchRuns(exp.getMlflowExperimentId(), null, null, 50);
                List<Map<String, Object>> metricsList = new ArrayList<>();
                if (runs != null) {
                    for (int i = 0; i < runs.size(); i++) {
                        JSONObject run = runs.getJSONObject(i);
                        JSONObject data = run.getJSONObject("data");
                        if (data != null) {
                            Map<String, Object> runMetrics = new LinkedHashMap<>();
                            runMetrics.put("runId", run.getJSONObject("info").getString("run_id"));
                            runMetrics.put("metrics", data.getJSONObject("metrics"));
                            runMetrics.put("params", data.getJSONObject("params"));
                            metricsList.add(runMetrics);
                        }
                    }
                }
                metrics.put("runs", metricsList);
            } catch (Exception e) {
                metrics.put("error", e.getMessage());
            }
        }
        return metrics;
    }

    public String createRun(Long experimentId, String runName, Map<String, String> params,
                            Map<String, Double> metrics) {
        Experiment exp = experimentMapper.selectById(experimentId);
        if (exp == null || exp.getMlflowExperimentId() == null) {
            throw new RuntimeException("Experiment not found or not linked to MLflow");
        }

        String runId = mlflowClient.createRun(exp.getMlflowExperimentId(), runName);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                mlflowClient.logParam(runId, entry.getKey(), entry.getValue());
            }
        }

        if (metrics != null) {
            long ts = System.currentTimeMillis();
            for (Map.Entry<String, Double> entry : metrics.entrySet()) {
                mlflowClient.logMetric(runId, entry.getKey(), entry.getValue(), ts, 0);
            }
        }

        return runId;
    }
}
