package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_operator_version")
public class OperatorVersion extends BaseEntity {
    private String operatorId;
    private Integer version;
    private String code;
    private String config;
}
