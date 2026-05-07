package com.aiplatform.lab.archive;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_archives")
public class ModelArchive extends BaseEntity {
    private String name;
    private String version;
    private String source;
    private String sourceId;
    private String description;
    private String status;
    private String metrics;
    private String framework;
}
