package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_feature_job")
public class FeatureJob extends BaseEntity {
    private String groupId;
    private String type;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
