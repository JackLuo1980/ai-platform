package com.aiplatform.console.tenant;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public R<Tenant> create(@RequestBody Tenant tenant) {
        return R.ok(tenantService.create(tenant));
    }

    @PutMapping("/{id}")
    public R<Tenant> update(@PathVariable Long id, @RequestBody Tenant tenant) {
        return R.ok(tenantService.update(id, tenant));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Tenant> getById(@PathVariable Long id) {
        return R.ok(tenantService.getById(id));
    }

    @GetMapping
    public R<PageResult<Tenant>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        return R.ok(tenantService.list(page, size, name, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id) {
        tenantService.toggleStatus(id);
        return R.ok();
    }

    @PutMapping("/{id}/quota")
    public R<Void> updateQuota(@PathVariable Long id, @RequestBody String quotaJson) {
        tenantService.updateQuota(id, quotaJson);
        return R.ok();
    }
}
