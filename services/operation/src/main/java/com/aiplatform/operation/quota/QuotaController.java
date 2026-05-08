package com.aiplatform.operation.quota;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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

    @PostMapping
    public R<ResourceQuota> create(@RequestBody ResourceQuota quota) {
        return R.ok(quotaService.create(quota));
    }

    @PutMapping("/{id}")
    public R<ResourceQuota> update(@PathVariable Long id, @RequestBody ResourceQuota quota) {
        return quotaService.update(id, quota);
    }

    @PostMapping("/check-preemption")
    public R<Map<String, Object>> checkPreemption(@RequestBody Map<String, Object> params) {
        Long quotaId = Long.valueOf(params.get("quotaId").toString());
        BigDecimal cpu = params.containsKey("cpu") ? new BigDecimal(params.get("cpu").toString()) : null;
        BigDecimal memory = params.containsKey("memory") ? new BigDecimal(params.get("memory").toString()) : null;
        BigDecimal gpu = params.containsKey("gpu") ? new BigDecimal(params.get("gpu").toString()) : null;
        boolean canPreempt = quotaService.checkPreemption(quotaId, cpu, memory, gpu);
        return R.ok(Map.of("canAllocate", canPreempt));
    }
}
