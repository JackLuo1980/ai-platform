package com.aiplatform.operation.environment;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/environments")
public class EnvironmentController {

    @Autowired
    private EnvironmentService environmentService;

    @PostMapping
    public R<Environment> create(@RequestBody Environment environment) {
        return R.ok(environmentService.create(environment));
    }

    @PutMapping("/{id}")
    public R<Environment> update(@PathVariable Long id, @RequestBody Environment environment) {
        return R.ok(environmentService.update(id, environment));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        environmentService.delete(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<Environment>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long imageId) {
        return R.ok(environmentService.list(page, size, imageId));
    }
}
