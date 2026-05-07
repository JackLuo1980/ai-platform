package com.aiplatform.fastlabel.export;

import com.aiplatform.common.model.R;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/exports")
@RequiredArgsConstructor
public class LabelExportController {

    private final LabelExportService exportService;
    private final LabelExportMapper exportMapper;


    @GetMapping
    public R<List<LabelExport>> list(@RequestParam(required = false) Long taskId,
                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<LabelExport> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(LabelExport::getTaskId, taskId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LabelExport::getStatus, status);
        }
        wrapper.orderByDesc(LabelExport::getCreatedAt);
        return R.ok(exportMapper.selectList(wrapper));
    }

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
