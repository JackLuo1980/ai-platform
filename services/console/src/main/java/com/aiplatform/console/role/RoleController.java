package com.aiplatform.console.role;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public R<Role> create(@RequestBody Role role) {
        return R.ok(roleService.create(role));
    }

    @PutMapping("/{id}")
    public R<Role> update(@PathVariable Long id, @RequestBody Role role) {
        return R.ok(roleService.update(id, role));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<Role>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long tenantId) {
        return R.ok(roleService.list(page, size, tenantId));
    }

    @PutMapping("/{id}/permissions")
    public R<Void> updatePermissions(@PathVariable Long id, @RequestBody List<RolePermission> permissions) {
        roleService.updatePermissions(id, permissions);
        return R.ok();
    }
}
