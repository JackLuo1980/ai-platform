package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dataset_versions")
public class DatasetVersion extends BaseEntity {
    private Long datasetId;
    private Integer version;
    private String storagePath;
    private String changeLog;
}
