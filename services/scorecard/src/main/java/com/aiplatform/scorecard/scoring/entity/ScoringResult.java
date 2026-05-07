package com.aiplatform.scorecard.scoring.entity;

import com.aiplatform.scorecard.common.BaseEntity;
import com.aiplatform.scorecard.common.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sc_scoring_results", autoResultMap = true)
public class ScoringResult extends BaseEntity {
    private Long modelId;
    private BigDecimal score;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String inputJson;
}
