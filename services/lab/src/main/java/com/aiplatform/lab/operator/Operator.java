package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_operator")
public class Operator extends BaseEntity {
    private String name;
    private String type;
    private String category;
    private String description;
    private String config;
}
