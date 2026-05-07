package com.aiplatform.fastlabel.task;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_tasks")
public class LabelTask extends BaseEntity {

    private Long datasetId;
    private String name;
    private String description;
    private String type;
    private String status;
    private String assignedTo;
    private Integer totalItems;
    private Integer labeledItems;
    private String createdBy;
}
