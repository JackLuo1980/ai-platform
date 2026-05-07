package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workflows")
public class Workflow extends BaseEntity {
    private Long projectId;
    private String name;
    private String type;
    private String nodesJson;
    private String edgesJson;
    private String status;
}
