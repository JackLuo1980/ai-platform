package com.aiplatform.lab.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_run_tasks")
public class WorkflowRunTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long runId;
    private String nodeId;
    private String nodeType;
    private String nodeName;
    private String status;
    private String image;
    private String paramsJson;
    private String resultJson;
    private String logPath;
    private String mlflowRunId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
