package com.aiplatform.lab.workflow.runner;

import com.aiplatform.lab.workflow.WorkflowRun;
import com.aiplatform.lab.workflow.WorkflowRunMapper;
import com.aiplatform.lab.workflow.entity.WorkflowRunTask;
import com.aiplatform.lab.workflow.mapper.WorkflowRunTaskMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineExecutor {

    private final WorkflowRunMapper runMapper;
    private final WorkflowRunTaskMapper taskMapper;
    private final TaskRunner taskRunner;

    public void executePipeline(Long runId, String nodesJson, String edgesJson) {
        WorkflowRun run = runMapper.selectById(runId);
        if (run == null) {
            log.error("Workflow run not found: {}", runId);
            return;
        }

        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        runMapper.updateById(run);

        try {
            JSONArray nodes = JSON.parseArray(nodesJson);
            JSONArray edges = edgesJson != null ? JSON.parseArray(edgesJson) : new JSONArray();

            Map<String, WorkflowRunTask> taskMap = new LinkedHashMap<>();
            Map<String, Set<String>> dependencies = new HashMap<>();

            if (nodes != null) {
                for (int i = 0; i < nodes.size(); i++) {
                    JSONObject node = nodes.getJSONObject(i);
                    String nodeId = node.getString("id");
                    String type = node.getString("type") != null ? node.getString("type") : "custom";
                    String name = node.getString("name") != null ? node.getString("name") : nodeId;
                    JSONObject config = node.getJSONObject("config");
                    String image = config != null && config.containsKey("image") ? config.getString("image") : null;
                    String params = config != null ? config.toString() : null;

                    WorkflowRunTask task = new WorkflowRunTask();
                    task.setRunId(runId);
                    task.setNodeId(nodeId);
                    task.setNodeType(type);
                    task.setNodeName(name);
                    task.setImage(image);
                    task.setParamsJson(params);
                    task.setStatus("PENDING");
                    taskMapper.insert(task);

                    taskMap.put(nodeId, task);
                    dependencies.put(nodeId, new HashSet<>());
                }
            }

            if (edges != null) {
                for (int i = 0; i < edges.size(); i++) {
                    JSONObject edge = edges.getJSONObject(i);
                    String source = edge.getString("source");
                    String target = edge.getString("target");
                    if (dependencies.containsKey(target)) {
                        dependencies.get(target).add(source);
                    }
                }
            }

            List<String> executionOrder = topologicalSort(taskMap.keySet(), dependencies);
            log.info("Pipeline execution order: {}", executionOrder);

            for (String nodeId : executionOrder) {
                WorkflowRunTask task = taskMap.get(nodeId);
                log.info("Executing node: {} ({})", task.getNodeName(), task.getNodeType());
                taskRunner.executeTask(task);
                taskMap.put(nodeId, task);

                if ("FAILED".equals(task.getStatus())) {
                    for (String remainingNodeId : executionOrder) {
                        WorkflowRunTask remaining = taskMap.get(remainingNodeId);
                        if ("PENDING".equals(remaining.getStatus())) {
                            remaining.setStatus("SKIPPED");
                            taskMapper.updateById(remaining);
                        }
                    }
                    break;
                }
            }

            boolean allCompleted = taskMap.values().stream()
                .allMatch(t -> "COMPLETED".equals(t.getStatus()));
            run.setStatus(allCompleted ? "COMPLETED" : "FAILED");

        } catch (Exception e) {
            log.error("Pipeline execution failed for run {}", runId, e);
            run.setStatus("FAILED");
        }

        run.setFinishedAt(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private List<String> topologicalSort(Set<String> nodes, Map<String, Set<String>> deps) {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String node : nodes) {
            visit(node, deps, visited, visiting, result);
        }
        return result;
    }

    private void visit(String node, Map<String, Set<String>> deps, Set<String> visited,
                       Set<String> visiting, List<String> result) {
        if (visited.contains(node)) return;
        if (visiting.contains(node)) return;

        visiting.add(node);
        for (String dep : deps.getOrDefault(node, Set.of())) {
            visit(dep, deps, visited, visiting, result);
        }
        visiting.remove(node);
        visited.add(node);
        result.add(node);
    }
}
