package com.aiplatform.inference.batch.controller;

import com.aiplatform.inference.common.R;
import com.aiplatform.inference.batch.entity.BatchService;
import com.aiplatform.inference.batch.service.BatchServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/batch")
@RequiredArgsConstructor
public class BatchServiceController {

    private final BatchServiceService batchServiceService;

    @PostMapping
    public R<BatchService> create(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        Long modelId = Long.valueOf(params.get("modelId").toString());
        String inputPath = (String) params.get("inputPath");
        String config = (String) params.getOrDefault("config", null);
        return R.ok(batchServiceService.create(name, modelId, inputPath, config));
    }

    @GetMapping("/{id}")
    public R<BatchService> getDetail(@PathVariable Long id) {
        return R.ok(batchServiceService.getById(id));
    }

    @PostMapping("/{id}/start")
    public R<BatchService> start(@PathVariable Long id) {
        return R.ok(batchServiceService.start(id));
    }

    @PostMapping("/{id}/stop")
    public R<BatchService> stop(@PathVariable Long id) {
        return R.ok(batchServiceService.stop(id));
    }

    @GetMapping("/{id}/results")
    public R<Map<String, String>> downloadResults(@PathVariable Long id) {
        String url = batchServiceService.getResultsDownloadUrl(id);
        return R.ok(Map.of("downloadUrl", url));
    }
}
