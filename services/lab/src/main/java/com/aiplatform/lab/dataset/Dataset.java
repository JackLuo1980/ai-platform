package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("datasets")
public class Dataset extends BaseEntity {
    private Long projectId;
    private String name;
    private String type;
    private Long sourceId;
    private String storagePath;
    private String schemaJson;
    private Long rowCount;
    private Long sizeBytes;
    private Integer version;
    private String description;
    private String status;
}
