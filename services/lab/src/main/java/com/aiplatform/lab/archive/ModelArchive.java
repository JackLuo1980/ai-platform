package com.aiplatform.lab.archive;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_archives")
public class ModelArchive extends BaseEntity {
    private Long projectId;
    private String name;
    private String format;
    private String artifactUri;
    private String runtimeImage;
    private String featureSchemaJson;
    private String evaluationSummaryJson;
    private String approvalStatus;
    private String sourceType;
    private Long sourceExperimentId;
    private Integer deleted;
}
