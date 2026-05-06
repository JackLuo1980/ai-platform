package com.aiplatform.inference.monitor.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_logs")
public class ServiceLog extends BaseEntity {

    private Long serviceId;
    private String serviceName;
    private String method;
    private String path;
    private Integer statusCode;
    private Long latencyMs;
    private String requestBody;
    private String responseBody;
    private String errorMessage;
    private String clientIp;
}
