package com.aiplatform.inference.monitor.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_metrics")
public class ServiceMetrics extends BaseEntity {

    private Long serviceId;
    private String serviceName;
    private BigDecimal qps;
    private BigDecimal avgLatencyMs;
    private BigDecimal p99LatencyMs;
    private BigDecimal errorRate;
    private Integer requestCount;
    private String period;
}
