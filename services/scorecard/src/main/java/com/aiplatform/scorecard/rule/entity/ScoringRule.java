package com.aiplatform.scorecard.rule.entity;

import com.aiplatform.scorecard.common.BaseEntity;
import com.aiplatform.scorecard.common.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sc_scoring_rules", autoResultMap = true)
public class ScoringRule extends BaseEntity {
    private Long modelId;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String variableRulesJson;
    private BigDecimal baseScore;
    private BigDecimal pdo;
}
