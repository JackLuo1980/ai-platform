package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lab/feature-definitions")
@RequiredArgsConstructor
public class FeatureDefinitionListController {

    private final FeatureStoreService featureStoreService;

    @GetMapping
    public R<List<FeatureDefinition>> list() {
        return R.ok(featureStoreService.listAllDefinitions());
    }
}
