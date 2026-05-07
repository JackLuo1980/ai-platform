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
    public R<ModelArchive> update(@PathVariable Long id, @RequestBody ModelArchive archive) {
        archive.setId(id);
        return R.ok(modelArchiveService.update(archive));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        modelArchiveService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<ModelArchive> get(@PathVariable Long id) {
        return R.ok(modelArchiveService.getById(id));
    }

    @GetMapping
    public R<PageResult<ModelArchive>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) Long tenantId,
                                            @RequestParam(required = false) String status) {
        return R.ok(modelArchiveService.list(tenantId, status, page, size));
    }

    @PostMapping("/{id}/submit")
    public R<ModelArchive> submitApproval(@PathVariable Long id) {
        return R.ok(modelArchiveService.submitApproval(id));
    }

    @PostMapping("/{id}/approve")
    public R<ModelArchive> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String comment = (String) body.get("comment");
        return R.ok(modelArchiveService.approve(id, approved, comment));
    }

    @GetMapping("/{id}/files")
    public R<List<ModelFile>> listFiles(@PathVariable Long id) {
        return R.ok(modelArchiveService.listFiles(id));
    }
}
