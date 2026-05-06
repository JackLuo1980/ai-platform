package com.aiplatform.fastlabel.item;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_item")
public class LabelItem extends BaseEntity {

    private Long taskId;
    private Long datasetId;
    private String dataPath;
    private String dataContent;
    private String status;
    private String annotationJson;
    private String assignedTo;
    private String reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;
}
