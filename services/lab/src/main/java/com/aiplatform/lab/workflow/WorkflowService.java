package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.workflow.runner.PipelineExecutor;
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
    private final PipelineExecutor pipelineExecutor;

    public Workflow create(Workflow workflow) {
        if (workflow.getNodesJson() != null) {
            validateDag(workflow.getNodesJson(), workflow.getEdgesJson());
        }
        workflowMapper.insert(workflow);
        return workflow;
    }

    public Workflow update(Workflow workflow) {
        if (workflow.getNodesJson() != null) {
            validateDag(workflow.getNodesJson(), workflow.getEdgesJson());
        }
        workflowMapper.updateById(workflow);
        return workflowMapper.selectById(workflow.getId());
    }

    public void delete(Long id) {
        workflowMapper.deleteById(id);
        workflowRunMapper.delete(new LambdaQueryWrapper<WorkflowRun>()
                .eq(WorkflowRun::getWorkflowId, id));
    }

    public Workflow getById(Long id) {
        return workflowMapper.selectById(id);
    }

    public PageResult<Workflow> list(Long tenantId, Long projectId, int page, int size) {
        LambdaQueryWrapper<Workflow> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Workflow::getTenantId, tenantId);
        if (projectId != null) wrapper.eq(Workflow::getProjectId, projectId);
        wrapper.orderByDesc(Workflow::getCreatedAt);
        IPage<Workflow> result = workflowMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public WorkflowRun run(Long id, Map<String, Object> params) {
        Workflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new RuntimeException("Workflow not found: " + id);
        }

        WorkflowRun run = new WorkflowRun();
        run.setWorkflowId(id);
        run.setStatus("PENDING");
        if (params != null && !params.isEmpty()) {
            run.setParamsJson(JSON.toJSONString(params));
        }
        workflowRunMapper.insert(run);

        pipelineExecutor.executePipeline(run.getId(), workflow.getNodesJson(), workflow.getEdgesJson());

        return workflowRunMapper.selectById(run.getId());
    }

    public PageResult<WorkflowRun> listRuns(Long workflowId, int page, int size) {
        LambdaQueryWrapper<WorkflowRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowRun::getWorkflowId, workflowId);
        wrapper.orderByDesc(WorkflowRun::getCreatedAt);
        IPage<WorkflowRun> result = workflowRunMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    private void validateDag(String nodesJson, String edgesJson) {
        if (nodesJson == null || nodesJson.isBlank()) return;
        JSONArray nodes = JSON.parseArray(nodesJson);
        JSONArray edges = edgesJson != null ? JSON.parseArray(edgesJson) : new JSONArray();
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
            if (detectCycle(node, adjacency, visited, recursionStack)) return true;
        }
        return false;
    }

    private boolean detectCycle(String node, Map<String, List<String>> adjacency,
                                Set<String> visited, Set<String> recursionStack) {
        visited.add(node);
        recursionStack.add(node);
        for (String neighbor : adjacency.getOrDefault(node, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                if (detectCycle(neighbor, adjacency, visited, recursionStack)) return true;
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }
        recursionStack.remove(node);
        return false;
    }
}
