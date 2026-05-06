package com.aiplatform.console.tenant;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    @Autowired
    private TenantMapper tenantMapper;

    public Tenant getById(Long id) {
        return tenantMapper.selectById(id);
    }

    public PageResult<Tenant> list(int page, int size, String name, String status) {
        Page<Tenant> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Tenant::getName, name);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Tenant::getStatus, status);
        }
        wrapper.orderByDesc(Tenant::getCreatedAt);
        Page<Tenant> result = tenantMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Tenant create(Tenant tenant) {
        tenant.setStatus("ACTIVE");
        tenantMapper.insert(tenant);
        return tenant;
    }

    public Tenant update(Long id, Tenant tenant) {
        tenant.setId(id);
        tenantMapper.updateById(tenant);
        return tenant;
    }

    public void toggleStatus(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant != null) {
            tenant.setStatus("ACTIVE".equals(tenant.getStatus()) ? "DISABLED" : "ACTIVE");
            tenantMapper.updateById(tenant);
        }
    }

    public void updateQuota(Long id, String quotaJson) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setQuotaJson(quotaJson);
        tenantMapper.updateById(tenant);
    }

    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }
}
