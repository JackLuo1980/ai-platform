package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import com.aiplatform.lab.workflow.entity.WorkflowRunTask;
import com.aiplatform.lab.workflow.mapper.WorkflowRunTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lab/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowRunTaskMapper workflowRunTaskMapper;

    @PostMapping
    public R<Workflow> create(@RequestBody Workflow workflow) {
        return R.ok(workflowService.create(workflow));
    }

    @PutMapping("/{id}")
    public R<Workflow> update(@PathVariable Long id, @RequestBody Workflow workflow) {
        workflow.setId(id);
        return R.ok(workflowService.update(workflow));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        workflowService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Workflow> get(@PathVariable Long id) {
        return R.ok(workflowService.getById(id));
    }

    @GetMapping
    public R<PageResult<Workflow>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) Long tenantId,
                                        @RequestParam(required = false) Long projectId) {
        return R.ok(workflowService.list(tenantId, projectId, page, size));
    }

    @PostMapping("/{id}/run")
    public R<WorkflowRun> run(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        return R.ok(workflowService.run(id, params));
    }

    @GetMapping("/{id}/runs")
    public R<PageResult<WorkflowRun>> listRuns(@PathVariable Long id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return R.ok(workflowService.listRuns(id, page, size));
    }

    @GetMapping("/{id}/runs/{runId}/tasks")
    public R<List<WorkflowRunTask>> listRunTasks(@PathVariable Long id, @PathVariable Long runId) {
        LambdaQueryWrapper<WorkflowRunTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowRunTask::getRunId, runId);
        wrapper.orderByAsc(WorkflowRunTask::getId);
        return R.ok(workflowRunTaskMapper.selectList(wrapper));
    }
}
