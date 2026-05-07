package com.aiplatform.lab.workflow.runner;

import com.aiplatform.lab.workflow.entity.WorkflowRunTask;
import com.aiplatform.lab.workflow.mapper.WorkflowRunTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRunner {

    private final WorkflowRunTaskMapper taskMapper;

    public void executeTask(WorkflowRunTask task) {
        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        try {
            log.info("Executing task: nodeId={}, type={}, image={}", task.getNodeId(), task.getNodeType(), task.getImage());

            String result = simulateExecution(task);
            task.setResultJson(result);
            task.setStatus("COMPLETED");
        } catch (Exception e) {
            log.error("Task execution failed: nodeId={}", task.getNodeId(), e);
            task.setStatus("FAILED");
            task.setResultJson("{\"error\":\"" + e.getMessage() + "\"}");
        }

        task.setFinishedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private String simulateExecution(WorkflowRunTask task) {
        StringBuilder result = new StringBuilder("{");
        result.append("\"nodeId\":\"").append(task.getNodeId()).append("\",");
        result.append("\"type\":\"").append(task.getNodeType()).append("\",");
        result.append("\"image\":\"").append(task.getImage() != null ? task.getImage() : "default").append("\",");
        result.append("\"duration_ms\":").append((long)(Math.random() * 5000 + 1000)).append(",");
        result.append("\"metrics\":{");
        if ("training".equals(task.getNodeType())) {
            result.append("\"accuracy\":").append(String.format("%.4f", 0.85 + Math.random() * 0.1)).append(",");
            result.append("\"loss\":").append(String.format("%.4f", 0.1 + Math.random() * 0.2));
        } else if ("evaluation".equals(task.getNodeType())) {
            result.append("\"auc\":").append(String.format("%.4f", 0.88 + Math.random() * 0.08)).append(",");
            result.append("\"f1\":").append(String.format("%.4f", 0.82 + Math.random() * 0.1));
        } else {
            result.append("\"status\":\"ok\"");
        }
        result.append("}}");
        return result.toString();
    }
}
