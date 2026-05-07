package com.aiplatform.inference.model.controller;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.common.R;
import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.service.InferenceModelService;
import com.aiplatform.inference.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/models")
@RequiredArgsConstructor
public class InferenceModelController {

    private final InferenceModelService modelService;
    private final VersionService versionService;

    @PostMapping("/import")
    public R<InferenceModel> importFromLab(@RequestBody Map<String, Object> params) {
        String labApiUrl = (String) params.get("labApiUrl");
        Long modelId = Long.valueOf(params.get("modelId").toString());
        String modelName = (String) params.get("modelName");
        String framework = (String) params.getOrDefault("framework", "pytorch");
        return R.ok(modelService.importFromLab(labApiUrl, modelId, modelName, framework));
    }

    @PostMapping("/upload")
    public R<InferenceModel> uploadModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelName") String modelName,
            @RequestParam(value = "framework", required = false) String framework,
            @RequestParam(value = "version", required = false) String version) {
        return R.ok(modelService.uploadModel(file, modelName, framework, version));
    }

    @GetMapping("/{id}")
    public R<InferenceModel> getDetail(@PathVariable Long id) {
        return R.ok(modelService.getById(id));
    }

    @PostMapping("/{id}/audit/approve")
    public R<InferenceModel> auditApprove(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        return R.ok(modelService.auditApprove(id, remark));
    }

    @PostMapping("/{id}/audit/reject")
    public R<InferenceModel> auditReject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        return R.ok(modelService.auditReject(id, remark));
    }


    @GetMapping("/{id}/versions")
    public R<java.util.List<InferenceModel>> getVersions(@PathVariable Long id) {
        return R.ok(modelService.listVersions(id));
    }


    @PostMapping("/{id}/rollback")
    public R<Map<String, Object>> rollbackModel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        var onlineServiceId = Long.valueOf(params.get("onlineServiceId").toString());
        var result = versionService.rollback(onlineServiceId, id);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("modelId", id);
        response.put("onlineService", result);
        return R.ok(response);
    }

    @GetMapping
    public R<PageResult<InferenceModel>> listModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(modelService.listModels(page, size, keyword));
    }
}
