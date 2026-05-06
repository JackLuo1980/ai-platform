package com.aiplatform.lab.archive;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lab/model-archives")
@RequiredArgsConstructor
public class ModelArchiveController {

    private final ModelArchiveService modelArchiveService;

    @PostMapping
    public R<ModelArchive> create(@RequestBody ModelArchive archive) {
        return R.ok(modelArchiveService.create(archive));
    }

    @PutMapping("/{id}")
    public R<ModelArchive> update(@PathVariable String id, @RequestBody ModelArchive archive) {
        archive.setId(id);
        return R.ok(modelArchiveService.update(archive));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        modelArchiveService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<ModelArchive> get(@PathVariable String id) {
        return R.ok(modelArchiveService.getById(id));
    }

    @GetMapping
    public R<PageResult<ModelArchive>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String tenantId,
                                            @RequestParam(required = false) String status) {
        return R.ok(modelArchiveService.list(tenantId, status, page, size));
    }

    @PostMapping("/import")
    public R<ModelArchive> importModel(@RequestBody Map<String, Object> body) {
        String tenantId = (String) body.get("tenantId");
        String name = (String) body.get("name");
        String version = (String) body.getOrDefault("version", "1.0.0");
        String framework = (String) body.get("framework");
        String description = (String) body.get("description");
        String metricsJson = body.get("metrics") != null ? body.get("metrics").toString() : null;
        @SuppressWarnings("unchecked")
        List<ModelFile> files = (List<ModelFile>) body.get("files");
        return R.ok(modelArchiveService.importThirdParty(tenantId, name, version, framework, description, metricsJson, files));
    }

    @PostMapping("/{id}/approve")
    public R<ModelArchive> approve(@PathVariable String id, @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String comment = (String) body.getOrDefault("comment", "");
        return R.ok(modelArchiveService.approve(id, approved, comment));
    }

    @PostMapping("/{id}/submit")
    public R<ModelArchive> submitApproval(@PathVariable String id) {
        return R.ok(modelArchiveService.submitApproval(id));
    }

    @GetMapping("/{id}/files")
    public R<List<ModelFile>> listFiles(@PathVariable String id) {
        return R.ok(modelArchiveService.listFiles(id));
    }
}
