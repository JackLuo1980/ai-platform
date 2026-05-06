package com.aiplatform.lab.workflow;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lab/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public R<Workflow> create(@RequestBody Workflow workflow) {
        return R.ok(workflowService.create(workflow));
    }

    @PutMapping("/{id}")
    public R<Workflow> update(@PathVariable String id, @RequestBody Workflow workflow) {
        workflow.setId(id);
        return R.ok(workflowService.update(workflow));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        workflowService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Workflow> get(@PathVariable String id) {
        return R.ok(workflowService.getById(id));
    }

    @GetMapping
    public R<PageResult<Workflow>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) String tenantId,
                                        @RequestParam(required = false) String projectId) {
        return R.ok(workflowService.list(tenantId, projectId, page, size));
    }

    @PostMapping("/{id}/run")
    public R<WorkflowRun> run(@PathVariable String id, @RequestBody(required = false) Map<String, Object> params) {
        return R.ok(workflowService.run(id, params));
    }

    @GetMapping("/{id}/runs")
    public R<PageResult<WorkflowRun>> listRuns(@PathVariable String id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return R.ok(workflowService.listRuns(id, page, size));
    }
}
