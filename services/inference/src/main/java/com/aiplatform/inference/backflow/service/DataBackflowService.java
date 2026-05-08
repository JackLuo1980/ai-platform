package com.aiplatform.inference.backflow.service;

import com.aiplatform.inference.backflow.entity.BackflowTask;
import com.aiplatform.inference.backflow.entity.PredictionRecord;
import com.aiplatform.inference.backflow.mapper.BackflowTaskMapper;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.mapper.OnlineServiceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataBackflowService {

    private final PredictionRecordService recordService;
    private final BackflowTaskMapper backflowTaskMapper;
    private final OnlineServiceMapper onlineServiceMapper;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${lab.api.url:http://10.0.0.2:8083}")
    private String labApiUrl;

    public BackflowTask executeBackflow(Long serviceId, String datasetName) {
        OnlineService service = onlineServiceMapper.selectById(serviceId);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }

        BackflowTask task = new BackflowTask();
        task.setServiceId(serviceId);
        task.setModelId(service.getModelId());
        task.setSourceType("PREDICTION_LOG");
        task.setTargetDatasetName(datasetName);
        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        backflowTaskMapper.insert(task);

        try {
            List<PredictionRecord> records = recordService.findByServiceId(serviceId, false, 10000);
            if (records.isEmpty()) {
                task.setStatus("COMPLETED");
                task.setRecordCount(0);
                task.setErrorMessage("No prediction records found for backflow");
                task.setCompletedAt(LocalDateTime.now());
                backflowTaskMapper.updateById(task);
                return task;
            }

            String csv = buildCsv(records);
            String storagePath = "backflow/" + serviceId + "/" + UUID.randomUUID() + ".csv";
            minioService.uploadFile(storagePath, csv.getBytes(StandardCharsets.UTF_8), "text/csv");

            Long targetDatasetId = createLabDataset(datasetName, service.getModelId(), storagePath, records.size());

            List<Long> recordIds = new ArrayList<>();
            for (PredictionRecord r : records) { recordIds.add(r.getId()); }
            recordService.markBackflowed(recordIds);

            task.setRecordCount(records.size());
            task.setStoragePath(storagePath);
            task.setTargetDatasetId(targetDatasetId);
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Backflow failed for service {}", serviceId, e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
        }

        backflowTaskMapper.updateById(task);
        return task;
    }

    private String buildCsv(List<PredictionRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,service_id,model_id,input_features,prediction_result,confidence,latency_ms,status,created_at\n");
        for (PredictionRecord r : records) {
            sb.append(r.getId()).append(",");
            sb.append(r.getServiceId()).append(",");
            sb.append(r.getModelId() != null ? r.getModelId() : "").append(",");
            sb.append(escapeCsv(r.getInputFeatures())).append(",");
            sb.append(escapeCsv(r.getPredictionResult())).append(",");
            sb.append(r.getConfidence() != null ? r.getConfidence() : "").append(",");
            sb.append(r.getLatencyMs() != null ? r.getLatencyMs() : "").append(",");
            sb.append(r.getStatus()).append(",");
            sb.append(r.getCreatedAt() != null ? r.getCreatedAt() : "").append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Long createLabDataset(String name, Long modelId, String storagePath, int recordCount) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("type", "backflow");
            body.put("description", "Data backflow from inference service, " + recordCount + " prediction records");
            body.put("storagePath", storagePath);
            body.put("source", "INFERENCE_BACKFLOW");
            body.put("rowCount", recordCount);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    labApiUrl + "/api/v1/lab/datasets", entity, Map.class);

            Map<String, Object> data = response.getBody();
            if (data != null && data.containsKey("data")) {
                Map<String, Object> inner = (Map<String, Object>) data.get("data");
                if (inner != null && inner.containsKey("id")) {
                    return Long.valueOf(inner.get("id").toString());
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to create lab dataset, continuing without target ID", e);
            return null;
        }
    }

    public List<BackflowTask> listTasks(Long serviceId) {
        LambdaQueryWrapper<BackflowTask> wrapper = new LambdaQueryWrapper<>();
        if (serviceId != null) {
            wrapper.eq(BackflowTask::getServiceId, serviceId);
        }
        wrapper.orderByDesc(BackflowTask::getCreatedAt);
        return backflowTaskMapper.selectList(wrapper);
    }

    public BackflowTask getTask(Long id) {
        return backflowTaskMapper.selectById(id);
    }
}
