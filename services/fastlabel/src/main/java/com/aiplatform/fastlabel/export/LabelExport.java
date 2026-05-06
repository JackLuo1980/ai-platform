package com.aiplatform.fastlabel.export;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_export")
public class LabelExport extends BaseEntity {

    private Long taskId;
    private Long datasetId;
    private String name;
    private String format;
    private String status;
    private String filePath;
    private Integer itemCount;
    private String exportedBy;
    private String errorMessage;
}
