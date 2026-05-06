package com.aiplatform.lab.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class MlflowClient {

    @Value("${mlflow.tracking-uri}")
    private String trackingUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createExperiment(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("name", name);
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(trackingUri + "/api/2.0/mlflow/experiments/create", entity, String.class);
        JSONObject result = JSON.parseObject(resp.getBody());
        return result.getString("experiment_id");
    }

    public String createRun(String experimentId, String runName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("experiment_id", experimentId);
        JSONObject runInfo = new JSONObject();
        runInfo.put("run_name", runName);
        body.put("start_time", System.currentTimeMillis());
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(trackingUri + "/api/2.0/mlflow/runs/create", entity, String.class);
        JSONObject result = JSON.parseObject(resp.getBody());
        JSONObject run = result.getJSONObject("run");
        JSONObject info = run.getJSONObject("info");
        return info.getString("run_id");
    }

    public void logParam(String runId, String key, String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("run_id", runId);
        body.put("key", key);
        body.put("value", value);
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        restTemplate.postForEntity(trackingUri + "/api/2.0/mlflow/runs/log-parameter", entity, String.class);
    }

    public void logMetric(String runId, String key, double value, long timestamp, int step) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("run_id", runId);
        body.put("key", key);
        body.put("value", value);
        body.put("timestamp", timestamp);
        body.put("step", step);
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        restTemplate.postForEntity(trackingUri + "/api/2.0/mlflow/runs/log-metric", entity, String.class);
    }

    public void logArtifact(String runId, String localPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("run_id", runId);
        body.put("path", localPath);
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        restTemplate.postForEntity(trackingUri + "/api/2.0/mlflow/artifacts/upload", entity, String.class);
    }

    public JSONObject getRun(String runId) {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                trackingUri + "/api/2.0/mlflow/runs/get?run_id=" + runId, String.class);
        return JSON.parseObject(resp.getBody());
    }

    public JSONArray searchRuns(String experimentId, String filter, String orderBy, int maxResults) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("experiment_ids", Collections.singletonList(experimentId));
        body.put("filter", filter != null ? filter : "");
        body.put("max_results", maxResults);
        if (orderBy != null) {
            body.put("order_by", Collections.singletonList(orderBy));
        }
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                trackingUri + "/api/2.0/mlflow/runs/search", entity, String.class);
        JSONObject result = JSON.parseObject(resp.getBody());
        return result.getJSONArray("runs");
    }

    public JSONObject getExperiment(String experimentId) {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                trackingUri + "/api/2.0/mlflow/experiments/get?experiment_id=" + experimentId, String.class);
        return JSON.parseObject(resp.getBody());
    }

    public JSONArray listExperiments() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                trackingUri + "/api/2.0/mlflow/experiments/search", String.class);
        JSONObject result = JSON.parseObject(resp.getBody());
        return result.getJSONArray("experiments");
    }
}
