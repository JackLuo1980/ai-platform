package com.aiplatform.lab.experiment;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experiments")
public class Experiment extends BaseEntity {
    private String projectId;
    private String name;
    private String mlflowExperimentId;
    private String description;
}
