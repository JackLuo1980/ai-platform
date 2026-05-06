package com.aiplatform.inference.evaluation.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_evaluations")
public class ModelEvaluation extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String modelVersion;
    private String evaluationType;
    private String template;
    private String status;
    private BigDecimal auc;
    private BigDecimal ks;
    private BigDecimal gini;
    private BigDecimal accuracy;
    private BigDecimal precision;
    private BigDecimal recall;
    private BigDecimal f1Score;
    private BigDecimal rmse;
    private BigDecimal mae;
    private BigDecimal r2;
    private BigDecimal mape;
    private BigDecimal map;
    private BigDecimal iou;
    private String confusionMatrix;
    private String rocCurve;
    private String details;
    private Integer sampleSize;
}
