package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lab/feature-jobs")
@RequiredArgsConstructor
public class FeatureJobController {

    private final FeatureStoreService featureStoreService;

    @GetMapping
    public R<PageResult<FeatureJob>> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String groupId,
                                          @RequestParam(required = false) String tenantId) {
        return R.ok(featureStoreService.listJobs(groupId, tenantId, page, size));
    }
}
