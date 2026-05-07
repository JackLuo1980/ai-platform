package com.aiplatform.lab.file;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_entries")
public class FileEntry extends BaseEntity {
    private Long projectId;
    private Long userId;
    private Long parentId;
    private String name;
    private String path;
    private String type;
    private String mimeType;
    private Long sizeBytes;
    private String storageKey;
    private String checksum;
    private String metadataJson;
    private String status;
}
