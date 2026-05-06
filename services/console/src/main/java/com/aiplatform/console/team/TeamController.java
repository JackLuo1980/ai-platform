package com.aiplatform.console.team;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping
    public R<AnnotationTeam> create(@RequestBody AnnotationTeam team) {
        return R.ok(teamService.create(team));
    }

    @PutMapping("/{id}")
    public R<AnnotationTeam> update(@PathVariable Long id, @RequestBody AnnotationTeam team) {
        return R.ok(teamService.update(id, team));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<AnnotationTeam>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long projectId) {
        return R.ok(teamService.list(page, size, tenantId, projectId));
    }

    @GetMapping("/{id}/members")
    public R<List<AnnotationTeamMember>> getMembers(@PathVariable Long id) {
        return R.ok(teamService.getMembers(id));
    }

    @PostMapping("/{id}/members")
    public R<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String role) {
        teamService.addMember(id, userId, role);
        return R.ok();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public R<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        teamService.removeMember(id, userId);
        return R.ok();
    }
}
