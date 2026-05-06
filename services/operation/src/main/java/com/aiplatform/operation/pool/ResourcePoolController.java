package com.aiplatform.operation.pool;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resource-pools")
public class ResourcePoolController {

    @Autowired
    private ResourcePoolService resourcePoolService;

    @PostMapping
    public R<ResourcePool> create(@RequestBody ResourcePool pool) {
        return R.ok(resourcePoolService.create(pool));
    }

    @PutMapping("/{id}")
    public R<ResourcePool> update(@PathVariable Long id, @RequestBody ResourcePool pool) {
        return R.ok(resourcePoolService.update(id, pool));
    }

    @GetMapping
    public R<PageResult<ResourcePool>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long clusterId) {
        return R.ok(resourcePoolService.list(page, size, clusterId));
    }
}
