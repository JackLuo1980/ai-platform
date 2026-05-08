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

    public ResourceQuota create(ResourceQuota quota) {
        if (quota.getPriority() == null) { quota.setPriority("MEDIUM"); }
        if (quota.getPreemptionPolicy() == null) { quota.setPreemptionPolicy("NONE"); }
        if (quota.getStatus() == null) { quota.setStatus("ACTIVE"); }
        if (quota.getCpuGuaranteed() == null) { quota.setCpuGuaranteed(BigDecimal.ZERO); }
        if (quota.getMemoryGuaranteed() == null) { quota.setMemoryGuaranteed(BigDecimal.ZERO); }
        if (quota.getGpuGuaranteed() == null) { quota.setGpuGuaranteed(BigDecimal.ZERO); }
        resourceQuotaMapper.insert(quota);
        return quota;
    }

    public boolean checkPreemption(Long requestingQuotaId, BigDecimal cpuNeeded, BigDecimal memoryNeeded, BigDecimal gpuNeeded) {
        ResourceQuota requester = resourceQuotaMapper.selectById(requestingQuotaId);
        if (requester == null) { return false; }
        if (!"PREEMPT_LOWER".equals(requester.getPreemptionPolicy())
                && !"PREEMPT_ALL".equals(requester.getPreemptionPolicy())) {
            return false;
        }

        ResourcePool pool = resourcePoolMapper.selectById(requester.getResourcePoolId());
        if (pool == null) { return false; }

        BigDecimal availableCpu = BigDecimal.ZERO;
        BigDecimal availableMemory = BigDecimal.ZERO;
        BigDecimal availableGpu = BigDecimal.ZERO;
        try {
            if (pool.getUsedCapacity() != null) {
                JsonNode used = objectMapper.readTree(pool.getUsedCapacity());
                JsonNode total = pool.getTotalCapacity() != null ? objectMapper.readTree(pool.getTotalCapacity()) : null;
                if (total != null) {
                    availableCpu = total.has("cpu") ? total.get("cpu").decimalValue().subtract(used.has("cpu") ? used.get("cpu").decimalValue() : BigDecimal.ZERO) : BigDecimal.ZERO;
                    availableMemory = total.has("memory") ? total.get("memory").decimalValue().subtract(used.has("memory") ? used.get("memory").decimalValue() : BigDecimal.ZERO) : BigDecimal.ZERO;
                    availableGpu = total.has("gpu") ? total.get("gpu").decimalValue().subtract(used.has("gpu") ? used.get("gpu").decimalValue() : BigDecimal.ZERO) : BigDecimal.ZERO;
                }
            }
        } catch (Exception e) { /* ignore */ }

        boolean sufficient = (cpuNeeded == null || availableCpu.compareTo(cpuNeeded) >= 0)
                && (memoryNeeded == null || availableMemory.compareTo(memoryNeeded) >= 0)
                && (gpuNeeded == null || availableGpu.compareTo(gpuNeeded) >= 0);
        if (sufficient) { return true; }

        LambdaQueryWrapper<ResourceQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceQuota::getResourcePoolId, requester.getResourcePoolId());
        wrapper.eq(ResourceQuota::getStatus, "ACTIVE");
        java.util.List<ResourceQuota> allQuotas = resourceQuotaMapper.selectList(wrapper);

        java.util.Map<String, Integer> priorityOrder = java.util.Map.of("HIGH", 3, "MEDIUM", 2, "LOW", 1);
        int requesterPriority = priorityOrder.getOrDefault(requester.getPriority(), 2);

        allQuotas.sort((a, b) -> priorityOrder.getOrDefault(a.getPriority(), 2) - priorityOrder.getOrDefault(b.getPriority(), 2));

        for (ResourceQuota q : allQuotas) {
            if (q.getId().equals(requestingQuotaId)) { continue; }
            int qPriority = priorityOrder.getOrDefault(q.getPriority(), 2);
            if ("PREEMPT_LOWER".equals(requester.getPreemptionPolicy()) && qPriority >= requesterPriority) {
                continue;
            }
            BigDecimal reclaimableCpu = q.getCpuGuaranteed() != null ? q.getCpuLimit().subtract(q.getCpuGuaranteed()) : q.getCpuLimit();
            BigDecimal reclaimableMemory = q.getMemoryGuaranteed() != null ? q.getMemoryLimit().subtract(q.getMemoryGuaranteed()) : q.getMemoryLimit();
            BigDecimal reclaimableGpu = q.getGpuGuaranteed() != null ? q.getGpuLimit().subtract(q.getGpuGuaranteed()) : q.getGpuLimit();

            availableCpu = availableCpu.add(reclaimableCpu != null ? reclaimableCpu : BigDecimal.ZERO);
            availableMemory = availableMemory.add(reclaimableMemory != null ? reclaimableMemory : BigDecimal.ZERO);
            availableGpu = availableGpu.add(reclaimableGpu != null ? reclaimableGpu : BigDecimal.ZERO);

            sufficient = (cpuNeeded == null || availableCpu.compareTo(cpuNeeded) >= 0)
                    && (memoryNeeded == null || availableMemory.compareTo(memoryNeeded) >= 0)
                    && (gpuNeeded == null || availableGpu.compareTo(gpuNeeded) >= 0);
            if (sufficient) { return true; }
        }
        return false;
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
