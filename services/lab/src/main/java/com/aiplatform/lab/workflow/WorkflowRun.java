package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workflow_runs")
public class WorkflowRun extends BaseEntity {
    private Long workflowId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String logPath;
    private String paramsJson;
    private String resultJson;
    private String mlflowRunId;
}
