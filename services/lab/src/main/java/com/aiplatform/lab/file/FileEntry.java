package com.aiplatform.lab.file;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_file_entry")
public class FileEntry extends BaseEntity {
    private String projectId;
    private String path;
    private String name;
    private Boolean isDirectory;
    private Long fileSize;
    private String storagePath;
}
