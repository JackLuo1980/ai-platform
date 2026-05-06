package com.aiplatform.inference.online.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("online_services")
public class OnlineService extends BaseEntity {

    private String name;
    private Long modelId;
    private String modelName;
    private String modelVersion;
    private String modelPath;
    private String status;
    private Integer replicas;
    private BigDecimal cpuCores;
    private Integer memoryMb;
    private Integer port;
    private String releaseType;
    private String config;
    private String k8sDeploymentName;
    private String k8sServiceName;
    private String endpoint;
}
