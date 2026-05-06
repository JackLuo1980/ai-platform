package com.aiplatform.inference.model.entity;

import com.aiplatform.inference.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inference_models")
public class InferenceModel extends BaseEntity {

    private String name;
    private String version;
    private String framework;
    private String modelType;
    private String filePath;
    private Long fileSize;
    private String description;
    private String status;
    private String sourceType;
    private Long sourceId;
    private String auditRemark;
}
