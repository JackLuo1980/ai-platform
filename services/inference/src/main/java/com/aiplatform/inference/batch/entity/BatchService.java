package com.aiplatform.inference.batch.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("batch_services")
public class BatchService extends BaseEntity {

    private String name;
    private Long modelId;
    private String modelName;
    private String modelVersion;
    private String status;
    private String inputPath;
    private String outputPath;
    private String resultPath;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer failedRecords;
    private String k8sJobName;
    private String config;
}
