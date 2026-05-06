package com.aiplatform.console.project;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public R<Project> create(@RequestBody Project project) {
        return R.ok(projectService.create(project));
    }

    @PutMapping("/{id}")
    public R<Project> update(@PathVariable Long id, @RequestBody Project project) {
        return R.ok(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<Project> getById(@PathVariable Long id) {
        return R.ok(projectService.getById(id));
    }

    @GetMapping
    public R<PageResult<Project>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String name) {
        return R.ok(projectService.list(page, size, tenantId, name));
    }

    @PostMapping("/{id}/members")
    public R<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Long roleId) {
        projectService.addMember(id, userId, roleId);
        return R.ok();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public R<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return R.ok();
    }
}
