package com.aiplatform.fastlabel.export;

import com.aiplatform.common.model.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/exports")
@RequiredArgsConstructor
public class LabelExportController {

    private final LabelExportService exportService;

    @PostMapping
    public R<LabelExport> createExport(@RequestBody Map<String, Object> body) {
        Long taskId = ((Number) body.get("taskId")).longValue();
        String format = (String) body.get("format");
        String exportedBy = (String) body.getOrDefault("exportedBy", "system");
        return R.ok(exportService.createExport(taskId, format, exportedBy));
    }

    @GetMapping("/{id}")
    public R<LabelExport> getById(@PathVariable Long id) {
        return R.ok(exportService.getById(id));
    }

    @PostMapping("/{id}/push")
    public R<Map<String, Object>> pushToLab(@PathVariable Long id) {
        return R.ok(exportService.pushToLab(id));
    }
}
