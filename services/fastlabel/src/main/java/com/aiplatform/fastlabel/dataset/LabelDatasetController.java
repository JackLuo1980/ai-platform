package com.aiplatform.fastlabel.dataset;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/datasets")
@RequiredArgsConstructor
public class LabelDatasetController {

    private final LabelDatasetService datasetService;

    @PostMapping
    public R<LabelDataset> create(@RequestBody LabelDataset dataset,
            @RequestHeader(value = "X-Tenant-Id", required = false) Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (tenantId != null) dataset.setTenantId(tenantId);
        return R.ok(datasetService.create(dataset));
    }

    @GetMapping
    public R<PageResult<LabelDataset>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return R.ok(datasetService.list(page, size, type, keyword));
    }

    @GetMapping("/{id}")
    public R<LabelDataset> getById(@PathVariable Long id) {
        return R.ok(datasetService.getById(id));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        datasetService.delete(id);
        return R.ok();
    }

    @PostMapping("/import")
    public R<LabelDataset> importFromLab(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String type = (String) body.get("type");
        Long sourceDatasetId = ((Number) body.get("sourceDatasetId")).longValue();
        String createdBy = (String) body.getOrDefault("createdBy", "system");
        return R.ok(datasetService.importFromLab(name, type, sourceDatasetId, createdBy));
    }

    @PostMapping("/upload")
    public R<LabelDataset> upload(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "system") String createdBy) {
        return R.ok(datasetService.upload(name, type, file, createdBy));
    }
}
