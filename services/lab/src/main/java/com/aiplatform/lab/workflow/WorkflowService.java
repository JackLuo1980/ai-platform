package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.PageResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowMapper workflowMapper;
    private final WorkflowRunMapper workflowRunMapper;

    public Workflow create(Workflow workflow) {
        validateDag(workflow.getDagJson());
        workflowMapper.insert(workflow);
        return workflow;
    }

    public Workflow update(Workflow workflow) {
        if (workflow.getDagJson() != null) {
            validateDag(workflow.getDagJson());
        }
        workflowMapper.updateById(workflow);
        return workflowMapper.selectById(workflow.getId());
    }

    public void delete(String id) {
        workflowMapper.deleteById(id);
        workflowRunMapper.delete(new LambdaQueryWrapper<WorkflowRun>()
                .eq(WorkflowRun::getWorkflowId, id));
    }

    public Workflow getById(String id) {
        return workflowMapper.selectById(id);
    }

    public PageResult<Workflow> list(String tenantId, String projectId, int page, int size) {
        LambdaQueryWrapper<Workflow> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Workflow::getTenantId, tenantId);
        if (projectId != null) wrapper.eq(Workflow::getProjectId, projectId);
        wrapper.orderByDesc(Workflow::getCreatedAt);
        IPage<Workflow> result = workflowMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public WorkflowRun run(String id, Map<String, Object> params) {
        Workflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new RuntimeException("Workflow not found: " + id);
        }

        WorkflowRun run = new WorkflowRun();
        run.setWorkflowId(id);
        run.setTenantId(workflow.getTenantId());
        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        workflowRunMapper.insert(run);

        try {
            List<String> executionOrder = topologicalSort(workflow.getDagJson());
            JSONArray results = new JSONArray();
            for (String nodeId : executionOrder) {
                JSONObject stepResult = new JSONObject();
                stepResult.put("nodeId", nodeId);
                stepResult.put("status", "COMPLETED");
                stepResult.put("timestamp", LocalDateTime.now().toString());
                results.add(stepResult);
            }
            run.setStatus("SUCCESS");
            run.setResult(results.toJSONString());
        } catch (Exception e) {
            run.setStatus("FAILED");
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            run.setResult(error.toJSONString());
        }

        run.setFinishedAt(LocalDateTime.now());
        workflowRunMapper.updateById(run);
        return run;
    }

    public PageResult<WorkflowRun> listRuns(String workflowId, int page, int size) {
        LambdaQueryWrapper<WorkflowRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowRun::getWorkflowId, workflowId);
        wrapper.orderByDesc(WorkflowRun::getCreatedAt);
        IPage<WorkflowRun> result = workflowRunMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    private void validateDag(String dagJson) {
        if (dagJson == null || dagJson.isBlank()) return;

        JSONObject dag = JSON.parseObject(dagJson);
        JSONArray nodes = dag.getJSONArray("nodes");
        JSONArray edges = dag.getJSONArray("edges");

        if (nodes == null || nodes.isEmpty()) return;

        Map<String, List<String>> adjacency = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            adjacency.put(nodes.getJSONObject(i).getString("id"), new ArrayList<>());
        }

        if (edges != null) {
            for (int i = 0; i < edges.size(); i++) {
                JSONObject edge = edges.getJSONObject(i);
                String source = edge.getString("source");
                String target = edge.getString("target");
                if (adjacency.containsKey(source)) {
                    adjacency.get(source).add(target);
                }
            }
        }

        if (hasCycle(adjacency)) {
            throw new RuntimeException("Workflow DAG contains a cycle");
        }
    }

    private boolean hasCycle(Map<String, List<String>> adjacency) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : adjacency.keySet()) {
            if (detectCycle(node, adjacency, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycle(String node, Map<String, List<String>> adjacency,
                                Set<String> visited, Set<String> recursionStack) {
        visited.add(node);
        recursionStack.add(node);

        for (String neighbor : adjacency.getOrDefault(node, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                if (detectCycle(neighbor, adjacency, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    private List<String> topologicalSort(String dagJson) {
        JSONObject dag = JSON.parseObject(dagJson);
        JSONArray nodes = dag.getJSONArray("nodes");
        JSONArray edges = dag.getJSONArray("edges");

        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            String id = nodes.getJSONObject(i).getString("id");
            adjacency.put(id, new ArrayList<>());
            inDegree.put(id, 0);
        }

        if (edges != null) {
            for (int i = 0; i < edges.size(); i++) {
                JSONObject edge = edges.getJSONObject(i);
                String source = edge.getString("source");
                String target = edge.getString("target");
                if (adjacency.containsKey(source) && adjacency.containsKey(target)) {
                    adjacency.get(source).add(target);
                    inDegree.merge(target, 1, Integer::sum);
                }
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);
            for (String neighbor : adjacency.get(current)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        return sorted;
    }
}
