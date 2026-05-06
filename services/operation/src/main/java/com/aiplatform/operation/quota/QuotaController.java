package com.aiplatform.operation.quota;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quotas")
public class QuotaController {

    @Autowired
    private QuotaService quotaService;

    @GetMapping
    public R<PageResult<ResourceQuota>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long projectId) {
        return R.ok(quotaService.list(page, size, tenantId, projectId));
    }

    @PutMapping("/{id}")
    public R<ResourceQuota> update(@PathVariable Long id, @RequestBody ResourceQuota quota) {
        return quotaService.update(id, quota);
    }
}
