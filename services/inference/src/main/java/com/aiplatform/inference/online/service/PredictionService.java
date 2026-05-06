package com.aiplatform.inference.online.service;

import com.aiplatform.inference.shared.ModelLoader;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.mapper.OnlineServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final OnlineServiceMapper onlineServiceMapper;
    private final MinioService minioService;
    private final ModelLoader modelLoader;

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

        return modelLoader.predict(service.getModelPath(), input);
    }
}
