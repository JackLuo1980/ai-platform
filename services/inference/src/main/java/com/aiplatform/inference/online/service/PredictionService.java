package com.aiplatform.inference.online.service;

import com.aiplatform.inference.backflow.entity.PredictionRecord;
import com.aiplatform.inference.backflow.service.PredictionRecordService;
import com.aiplatform.inference.shared.ModelLoader;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.mapper.OnlineServiceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final OnlineServiceMapper onlineServiceMapper;
    private final MinioService minioService;
    private final ModelLoader modelLoader;
    private final PredictionRecordService recordService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> predict(Long serviceId, Map<String, Object> input) {
        OnlineService service = onlineServiceMapper.selectById(serviceId);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }
        if (!"RUNNING".equals(service.getStatus())) {
            throw new RuntimeException("Service is not running");
        }

        if (!modelLoader.isLoaded(service.getModelPath())) {
            byte[] modelData = minioService.downloadFileBytes(service.getModelPath());
            modelLoader.loadModel(service.getModelPath(), modelData);
        }

        long startTime = System.currentTimeMillis();
        PredictionRecord record = new PredictionRecord();
        record.setServiceId(serviceId);
        record.setModelId(service.getModelId());
        record.setBackflowed(false);

        try {
            record.setInputFeatures(objectMapper.writeValueAsString(input));
        } catch (Exception e) {
            record.setInputFeatures(String.valueOf(input));
        }

        try {
            Map<String, Object> result = modelLoader.predict(service.getModelPath(), input);
            long latency = System.currentTimeMillis() - startTime;
            record.setLatencyMs(latency);
            record.setStatus("SUCCESS");
            try {
                record.setPredictionResult(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                record.setPredictionResult(String.valueOf(result));
            }
            if (result.containsKey("confidence")) {
                Object conf = result.get("confidence");
                record.setConfidence(new BigDecimal(conf.toString()));
            }
            recordService.save(record);
            return result;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            record.setLatencyMs(latency);
            record.setStatus("ERROR");
            record.setErrorMessage(e.getMessage());
            recordService.save(record);
            throw e;
        }
    }
}
