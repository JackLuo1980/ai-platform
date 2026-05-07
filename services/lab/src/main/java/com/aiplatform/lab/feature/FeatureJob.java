package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feature_jobs")
public class FeatureJob extends BaseEntity {
    private Long groupId;
    private String jobType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long rowsProcessed;
    private String errorMessage;
}
