package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lab/feature-groups/{groupId}/definitions")
@RequiredArgsConstructor
public class FeatureDefinitionController {

    private final FeatureStoreService featureStoreService;

    @GetMapping
    public R<List<FeatureDefinition>> list(@PathVariable String groupId) {
        return R.ok(featureStoreService.listDefinitions(groupId));
    }

    @PostMapping
    public R<FeatureDefinition> add(@PathVariable String groupId, @RequestBody FeatureDefinition definition) {
        definition.setGroupId(groupId);
        return R.ok(featureStoreService.addDefinition(definition));
    }
}
