package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operators")
public class Operator extends BaseEntity {
    private String name;
    private String type;
    private String category;
    private String description;
    private String paramsSchemaJson;
    private String code;
    private Integer version;
    private Boolean isShared;
}
