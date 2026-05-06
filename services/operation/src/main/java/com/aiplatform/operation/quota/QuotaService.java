package com.aiplatform.operation.quota;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import com.aiplatform.operation.pool.ResourcePool;
import com.aiplatform.operation.pool.ResourcePoolMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QuotaService {

    @Autowired
    private ResourceQuotaMapper resourceQuotaMapper;

    @Autowired
    private ResourcePoolMapper resourcePoolMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResult<ResourceQuota> list(int page, int size, Long tenantId, Long projectId) {
        Page<ResourceQuota> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<ResourceQuota> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(ResourceQuota::getTenantId, tenantId);
        }
        if (projectId != null) {
            wrapper.eq(ResourceQuota::getProjectId, projectId);
        }
        wrapper.orderByDesc(ResourceQuota::getCreatedAt);
        Page<ResourceQuota> result = resourceQuotaMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public R<ResourceQuota> update(Long id, ResourceQuota quota) {
        R<Void> validation = validateQuota(quota);
        if (validation.getCode() != 200) {
            return R.fail(validation.getCode(), validation.getMessage());
        }

        quota.setId(id);
        resourceQuotaMapper.updateById(quota);
        return R.ok(quota);
    }

    private R<Void> validateQuota(ResourceQuota quota) {
        if (quota.getResourcePoolId() == null) {
            return R.ok();
        }

        ResourcePool pool = resourcePoolMapper.selectById(quota.getResourcePoolId());
        if (pool == null) {
            return R.fail(404, "Resource pool not found");
        }

        try {
            if (pool.getTotalCapacity() != null) {
                JsonNode capacity = objectMapper.readTree(pool.getTotalCapacity());
                BigDecimal maxCpu = capacity.has("cpu") ? capacity.get("cpu").decimalValue() : null;
                BigDecimal maxGpu = capacity.has("gpu") ? capacity.get("gpu").decimalValue() : null;
                BigDecimal maxMemory = capacity.has("memory") ? capacity.get("memory").decimalValue() : null;

                if (quota.getCpuLimit() != null && maxCpu != null && quota.getCpuLimit().compareTo(maxCpu) > 0) {
                    return R.fail(400, "CPU limit exceeds pool capacity");
                }
                if (quota.getGpuLimit() != null && maxGpu != null && quota.getGpuLimit().compareTo(maxGpu) > 0) {
                    return R.fail(400, "GPU limit exceeds pool capacity");
                }
                if (quota.getMemoryLimit() != null && maxMemory != null && quota.getMemoryLimit().compareTo(maxMemory) > 0) {
                    return R.fail(400, "Memory limit exceeds pool capacity");
                }
            }
        } catch (Exception e) {
            // skip JSON parsing errors
        }

        return R.ok();
    }
}
