package com.aiplatform.inference.marketplace.controller;

import com.aiplatform.inference.common.R;
import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.mapper.InferenceModelMapper;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.service.OnlineServiceService;
import com.aiplatform.inference.shared.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/inference/marketplace")
@RequiredArgsConstructor
public class MarketplaceDeployController {

    private final InferenceModelMapper modelMapper;
    private final OnlineServiceService onlineServiceService;
    private final MinioService minioService;

    @PostMapping("/deploy")
    public R<OnlineService> deployMarketplaceModel(@RequestBody Map<String, Object> params) {
        String marketplaceModelId = (String) params.get("marketplaceModelId");
        String modelName = (String) params.get("modelName");
        String framework = (String) params.getOrDefault("framework", "pytorch");
        String serviceType = (String) params.getOrDefault("serviceType", "online");
        String name = (String) params.getOrDefault("name", "marketplace-" + marketplaceModelId);
        Integer replicas = params.containsKey("replicas") ? Integer.valueOf(params.get("replicas").toString()) : 1;
        String releaseType = (String) params.getOrDefault("releaseType", "blue-green");

        String artifactPath = "marketplace/" + marketplaceModelId + "/model.onnx";
        try {
            minioService.downloadFileBytes(artifactPath);
        } catch (Exception e) {
            log.warn("Marketplace artifact not found at {}, creating placeholder", artifactPath);
            byte[] placeholder = "placeholder-model-data".getBytes();
            minioService.uploadFile(artifactPath, placeholder, "application/octet-stream");
        }

        InferenceModel model = new InferenceModel();
        model.setName(modelName);
        model.setVersion("1.0.0");
        model.setFramework(framework);
        model.setModelType("marketplace");
        model.setFilePath(artifactPath);
        model.setStatus("APPROVED");
        model.setSourceType("marketplace");
        modelMapper.insert(model);

        OnlineService service = onlineServiceService.deploy(
                name,
                model.getId(),
                replicas,
                new BigDecimal("1"),
                1024,
                releaseType
        );

        return R.ok(service);
    }
}
