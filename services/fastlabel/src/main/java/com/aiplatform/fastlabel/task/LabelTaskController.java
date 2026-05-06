package com.aiplatform.fastlabel.task;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import com.aiplatform.fastlabel.item.LabelItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/tasks")
@RequiredArgsConstructor
public class LabelTaskController {

    private final LabelTaskService taskService;

    @PostMapping
    public R<LabelTask> create(@RequestBody LabelTask task) {
        return R.ok(taskService.create(task));
    }

    @GetMapping
    public R<PageResult<LabelTask>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long datasetId) {
        return R.ok(taskService.list(page, size, status, datasetId));
    }

    @GetMapping("/{id}")
    public R<LabelTask> getById(@PathVariable Long id) {
        return R.ok(taskService.getById(id));
    }

    @PutMapping("/{id}/assign")
    public R<LabelTask> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(taskService.assign(id, body.get("assignedTo")));
    }

    @GetMapping("/{id}/items")
    public R<PageResult<LabelItem>> getItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(taskService.getItems(id, page, size));
    }

    @PutMapping("/{id}/complete")
    public R<LabelTask> complete(@PathVariable Long id) {
        return R.ok(taskService.completeTask(id));
    }
}
