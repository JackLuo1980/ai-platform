package com.aiplatform.lab.archive;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_files")
public class ModelFile extends BaseEntity {
    private String archiveId;
    private String fileName;
    private String filePath;
    private Long fileSize;
}
