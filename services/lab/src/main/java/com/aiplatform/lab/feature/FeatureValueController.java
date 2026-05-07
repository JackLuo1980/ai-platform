package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lab/feature-groups/{groupId}/values")
@RequiredArgsConstructor
public class FeatureValueController {

    private final FeatureStoreService featureStoreService;

    @PostMapping("/query")
    public R<Map<String, Object>> pointInTimeQuery(@PathVariable Long groupId,
                                                    @RequestBody Map<String, Object> body) {
        String entityKey = (String) body.get("entityKey");
        String timestampStr = (String) body.getOrDefault("timestamp", LocalDateTime.now().toString());
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr);
        return R.ok(featureStoreService.pointInTimeQuery(groupId, entityKey, timestamp));
    }

    @PostMapping("/batch-query")
    public R<List<Map<String, Object>>> batchPointInTimeQuery(@PathVariable Long groupId,
                                                               @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> entityKeys = (List<String>) body.get("entityKeys");
        String timestampStr = (String) body.getOrDefault("timestamp", LocalDateTime.now().toString());
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr);
        return R.ok(featureStoreService.batchPointInTimeQuery(groupId, entityKeys, timestamp));
    }

    @PostMapping("/compute")
    public R<FeatureJob> triggerCompute(@PathVariable Long groupId,
                                         @RequestParam Long tenantId) {
        return R.ok(featureStoreService.triggerCompute(groupId, tenantId));
    }

    @GetMapping("/online")
    public R<Map<String, String>> getOnline(@PathVariable Long groupId,
                                             @RequestParam Long tenantId,
                                             @RequestParam String entityKey) {
        return R.ok(featureStoreService.getFromOnline(tenantId, groupId, entityKey));
    }

    @PostMapping("/offline")
    public R<Void> saveOffline(@PathVariable Long groupId,
                               @RequestParam Long tenantId,
                               @RequestBody List<Map<String, String>> values) {
        featureStoreService.saveOfflineValues(tenantId, groupId, values);
        return R.ok();
    }
}
