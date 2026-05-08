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

    private String priority;

    @TableField("cpu_guaranteed")
    private BigDecimal cpuGuaranteed;

    @TableField("memory_guaranteed")
    private BigDecimal memoryGuaranteed;

    @TableField("gpu_guaranteed")
    private BigDecimal gpuGuaranteed;

    private String preemptionPolicy;

    private String status;

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

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public BigDecimal getCpuGuaranteed() { return cpuGuaranteed; }
    public void setCpuGuaranteed(BigDecimal cpuGuaranteed) { this.cpuGuaranteed = cpuGuaranteed; }

    public BigDecimal getMemoryGuaranteed() { return memoryGuaranteed; }
    public void setMemoryGuaranteed(BigDecimal memoryGuaranteed) { this.memoryGuaranteed = memoryGuaranteed; }

    public BigDecimal getGpuGuaranteed() { return gpuGuaranteed; }
    public void setGpuGuaranteed(BigDecimal gpuGuaranteed) { this.gpuGuaranteed = gpuGuaranteed; }

    public String getPreemptionPolicy() { return preemptionPolicy; }
    public void setPreemptionPolicy(String preemptionPolicy) { this.preemptionPolicy = preemptionPolicy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
