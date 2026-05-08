package com.aiplatform.inference.backflow.controller;

import com.aiplatform.inference.backflow.entity.BackflowTask;
import com.aiplatform.inference.backflow.service.DataBackflowService;
import com.aiplatform.inference.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/backflow")
@RequiredArgsConstructor
public class BackflowController {

    private final DataBackflowService backflowService;

    @PostMapping("/execute")
    public R<BackflowTask> execute(@RequestBody Map<String, Object> params) {
        Long serviceId = Long.valueOf(params.get("serviceId").toString());
        String datasetName = (String) params.getOrDefault("datasetName",
                "Backflow_" + serviceId + "_" + System.currentTimeMillis());
        return R.ok(backflowService.executeBackflow(serviceId, datasetName));
    }

    @GetMapping("/tasks")
    public R<?> listTasks(@RequestParam(required = false) Long serviceId) {
        return R.ok(backflowService.listTasks(serviceId));
    }

    @GetMapping("/tasks/{id}")
    public R<BackflowTask> getTask(@PathVariable Long id) {
        return R.ok(backflowService.getTask(id));
    }
}
