package com.aiplatform.scorecard.model.entity;

import com.aiplatform.scorecard.common.BaseEntity;
import com.aiplatform.scorecard.common.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sc_models", autoResultMap = true)
public class ScModel extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private String name;
    private Long datasetId;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String selectedVariablesJson;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String binningConfigJson;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String coefficientsJson;
    private BigDecimal intercept;
    private BigDecimal ksValue;
    private BigDecimal aucValue;
    private BigDecimal giniValue;
    private BigDecimal psiValue;
    private String reportUri;
    private String status;
}
