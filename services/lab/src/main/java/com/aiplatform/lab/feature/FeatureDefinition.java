package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feature_definitions")
public class FeatureDefinition extends BaseEntity {
    private Long groupId;
    private String name;
    private String dtype;
    private String description;
    private String defaultValue;
}
