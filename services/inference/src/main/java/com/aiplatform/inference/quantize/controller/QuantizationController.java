package com.aiplatform.inference.quantize.controller;

import com.aiplatform.inference.common.R;
import com.aiplatform.inference.quantize.entity.ModelQuantization;
import com.aiplatform.inference.quantize.service.QuantizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/quantize")
@RequiredArgsConstructor
public class QuantizationController {

    private final QuantizationService quantizationService;

    @PostMapping
    public R<ModelQuantization> quantize(@RequestBody Map<String, Object> params) {
        Long modelId = Long.valueOf(params.get("modelId").toString());
        String quantizationType = (String) params.getOrDefault("quantizationType", "INT8");
        return R.ok(quantizationService.quantize(modelId, quantizationType));
    }

    @GetMapping("/{id}/result")
    public R<ModelQuantization> getResult(@PathVariable Long id) {
        return R.ok(quantizationService.getResult(id));
    }
}
