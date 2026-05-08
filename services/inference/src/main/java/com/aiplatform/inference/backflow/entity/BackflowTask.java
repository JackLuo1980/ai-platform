package com.aiplatform.inference.backflow.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_backflow_tasks")
public class BackflowTask extends BaseEntity {

    private Long serviceId;
    private Long modelId;
    private String sourceType;
    private Integer recordCount;
    private String status;
    private Long targetDatasetId;
    private String targetDatasetName;
    private String storagePath;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
