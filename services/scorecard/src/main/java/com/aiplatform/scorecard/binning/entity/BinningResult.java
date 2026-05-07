package com.aiplatform.scorecard.binning.entity;

import com.aiplatform.scorecard.common.JsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName(value = "sc_binning_results", autoResultMap = true)
public class BinningResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long variableId;
    private String method;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String binsJson;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String woeJson;
    private BigDecimal ivValue;
}
