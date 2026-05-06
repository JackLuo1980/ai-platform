package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_dataset")
public class Dataset extends BaseEntity {
    private String projectId;
    private String name;
    private String description;
    private String sourceType;
    private String sourceId;
    private String schema;
    private Long rowCount;
    private Long fileSize;
    private String storagePath;
}
