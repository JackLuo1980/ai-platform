package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lab/feature-groups")
@RequiredArgsConstructor
public class FeatureGroupController {

    private final FeatureStoreService featureStoreService;

    @PostMapping
    public R<FeatureGroup> create(@RequestBody FeatureGroup group) {
        return R.ok(featureStoreService.createGroup(group));
    }

    @PutMapping("/{id}")
    public R<FeatureGroup> update(@PathVariable String id, @RequestBody FeatureGroup group) {
        group.setId(id);
        return R.ok(featureStoreService.updateGroup(group));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        featureStoreService.deleteGroup(id);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<FeatureGroup> get(@PathVariable String id) {
        return R.ok(featureStoreService.getGroup(id));
    }

    @GetMapping
    public R<PageResult<FeatureGroup>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String tenantId) {
        return R.ok(featureStoreService.listGroups(tenantId, page, size));
    }
}
