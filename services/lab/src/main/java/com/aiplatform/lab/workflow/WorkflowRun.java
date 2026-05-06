package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lab_workflow_run")
public class WorkflowRun extends BaseEntity {
    private String workflowId;
    private String status;
    private String result;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
