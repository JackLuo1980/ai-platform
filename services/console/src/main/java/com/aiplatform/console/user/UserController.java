package com.aiplatform.console.user;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public R<User> create(@RequestBody User user) {
        return R.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    public R<User> update(@PathVariable Long id, @RequestBody User user) {
        return R.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<User>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String username) {
        return R.ok(userService.list(page, size, tenantId, username));
    }
}
