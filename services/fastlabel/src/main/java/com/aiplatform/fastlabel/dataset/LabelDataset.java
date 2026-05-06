package com.aiplatform.fastlabel.dataset;

import com.aiplatform.common.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("label_dataset")
public class LabelDataset extends BaseEntity {

    private String name;
    private String description;
    private String type;
    private String source;
    private Long sourceDatasetId;
    private Integer itemCount;
    private String status;
    private String createdBy;
}
