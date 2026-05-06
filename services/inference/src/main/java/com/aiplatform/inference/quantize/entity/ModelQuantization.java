package com.aiplatform.inference.quantize.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_quantizations")
public class ModelQuantization extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String originalPath;
    private String quantizedPath;
    private String quantizationType;
    private String status;
    private BigDecimal originalSizeMb;
    private BigDecimal quantizedSizeMb;
    private BigDecimal compressionRatio;
    private BigDecimal accuracyLoss;
    private BigDecimal latencyImprovement;
    private String details;
}
