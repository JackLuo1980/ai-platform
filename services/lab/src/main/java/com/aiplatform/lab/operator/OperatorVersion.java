package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operator_versions")
public class OperatorVersion extends BaseEntity {
    private String operatorId;
    private Integer version;
    private String code;
    private String config;
}
