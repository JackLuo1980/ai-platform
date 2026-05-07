package com.aiplatform.lab.quality;

import com.aiplatform.lab.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lab/data-quality")
@RequiredArgsConstructor
public class DataQualityController {

    private final DataQualityService dataQualityService;

    @PostMapping("/validate")
    public R<Map<String, Object>> validate(@RequestBody Map<String, Object> body) {
        Long datasetId = Long.parseLong(body.get("datasetId").toString());
        int version = body.containsKey("version") ? ((Number) body.get("version")).intValue() : 1;
        @SuppressWarnings("unchecked")
        Map<String, Object> rules = (Map<String, Object>) body.getOrDefault("rules", Map.of());
        return R.ok(dataQualityService.validate(datasetId, version, rules));
    }

    @GetMapping("/score")
    public R<Map<String, Object>> score(@RequestParam Long datasetId,
                                        @RequestParam(defaultValue = "1") int version) {
        return R.ok(dataQualityService.score(datasetId, version));
    }
}
