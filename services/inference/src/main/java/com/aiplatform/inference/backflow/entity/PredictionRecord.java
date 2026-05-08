package com.aiplatform.inference.backflow.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prediction_records")
public class PredictionRecord extends BaseEntity {

    private Long serviceId;
    private Long modelId;
    private String inputFeatures;
    private String predictionResult;
    private BigDecimal confidence;
    private Long latencyMs;
    private String status;
    private String errorMessage;
    private Boolean backflowed;
}
