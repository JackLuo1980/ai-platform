package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feature_groups")
public class FeatureGroup extends BaseEntity {
    private String name;
    private String description;
    private String entityKey;
    private String schedule;
}
