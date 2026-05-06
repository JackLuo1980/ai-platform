package com.aiplatform.inference.online.controller;

import com.aiplatform.inference.common.R;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.service.OnlineServiceService;
import com.aiplatform.inference.online.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/online")
@RequiredArgsConstructor
public class OnlineServiceController {

    private final OnlineServiceService onlineServiceService;
    private final PredictionService predictionService;

    @PostMapping("/deploy")
    public R<OnlineService> deploy(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        Long modelId = Long.valueOf(params.get("modelId").toString());
        Integer replicas = params.containsKey("replicas") ? Integer.valueOf(params.get("replicas").toString()) : null;
        BigDecimal cpuCores = params.containsKey("cpuCores") ? new BigDecimal(params.get("cpuCores").toString()) : null;
        Integer memoryMb = params.containsKey("memoryMb") ? Integer.valueOf(params.get("memoryMb").toString()) : null;
        String releaseType = (String) params.getOrDefault("releaseType", "canary");
        return R.ok(onlineServiceService.deploy(name, modelId, replicas, cpuCores, memoryMb, releaseType));
    }

    @GetMapping("/{id}")
    public R<OnlineService> getDetail(@PathVariable Long id) {
        return R.ok(onlineServiceService.getById(id));
    }

    @PutMapping("/{id}/config")
    public R<OnlineService> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer replicas = params.containsKey("replicas") ? Integer.valueOf(params.get("replicas").toString()) : null;
        BigDecimal cpuCores = params.containsKey("cpuCores") ? new BigDecimal(params.get("cpuCores").toString()) : null;
        Integer memoryMb = params.containsKey("memoryMb") ? Integer.valueOf(params.get("memoryMb").toString()) : null;
        return R.ok(onlineServiceService.updateConfig(id, replicas, cpuCores, memoryMb));
    }

    @PostMapping("/{id}/stop")
    public R<Void> stop(@PathVariable Long id) {
        onlineServiceService.stop(id);
        return R.ok();
    }

    @PostMapping("/{id}/predict")
    public R<Map<String, Object>> predict(@PathVariable Long id, @RequestBody Map<String, Object> input) {
        return R.ok(predictionService.predict(id, input));
    }

    @PutMapping("/{id}/release-type")
    public R<Void> toggleReleaseType(@PathVariable Long id, @RequestBody Map<String, String> params) {
        onlineServiceService.toggleReleaseType(id, params.get("releaseType"));
        return R.ok();
    }
}
