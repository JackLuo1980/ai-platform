package com.aiplatform.scorecard.variable.entity;

import com.aiplatform.scorecard.common.BaseEntity;
import com.aiplatform.scorecard.common.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sc_variables", autoResultMap = true)
public class ScVariable extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private Long datasetId;
    private String name;
    private String dtype;
    private BigDecimal ivValue;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String woeJson;
    private BigDecimal missingRate;
}
