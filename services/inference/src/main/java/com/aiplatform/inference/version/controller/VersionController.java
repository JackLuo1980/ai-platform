package com.aiplatform.inference.version.controller;

import com.aiplatform.inference.common.R;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping("/compare")
    public R<Map<String, Object>> compare(@RequestBody Map<String, Object> params) {
        Long modelIdA = Long.valueOf(params.get("modelIdA").toString());
        Long modelIdB = Long.valueOf(params.get("modelIdB").toString());
        return R.ok(versionService.compare(modelIdA, modelIdB));
    }

    @PostMapping("/rollback")
    public R<OnlineService> rollback(@RequestBody Map<String, Object> params) {
        Long onlineServiceId = Long.valueOf(params.get("onlineServiceId").toString());
        Long targetModelId = Long.valueOf(params.get("targetModelId").toString());
        return R.ok(versionService.rollback(onlineServiceId, targetModelId));
    }
}
