package com.aiplatform.operation.quota;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("resource_quotas")
public class ResourceQuota extends BaseEntity {

    private Long tenantId;
    private Long projectId;

    @TableField("resource_pool_id")
    private Long resourcePoolId;

    @TableField("cpu_limit")
    private BigDecimal cpuLimit;

    @TableField("memory_limit")
    private BigDecimal memoryLimit;

    @TableField("gpu_limit")
    private BigDecimal gpuLimit;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getResourcePoolId() {
        return resourcePoolId;
    }

    public void setResourcePoolId(Long resourcePoolId) {
        this.resourcePoolId = resourcePoolId;
    }

    public BigDecimal getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(BigDecimal cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public BigDecimal getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(BigDecimal memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public BigDecimal getGpuLimit() {
        return gpuLimit;
    }

    public void setGpuLimit(BigDecimal gpuLimit) {
        this.gpuLimit = gpuLimit;
    }
}
