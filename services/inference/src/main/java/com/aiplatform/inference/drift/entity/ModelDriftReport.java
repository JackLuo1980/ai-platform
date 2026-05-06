package com.aiplatform.inference.drift.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_drift_reports")
public class ModelDriftReport extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String modelVersion;
    private String driftType;
    private String status;
    private BigDecimal driftScore;
    private BigDecimal threshold;
    private Boolean isDrifted;
    private String details;
    private Long sampleSize;
    private String baselinePeriod;
    private String detectionPeriod;
}
