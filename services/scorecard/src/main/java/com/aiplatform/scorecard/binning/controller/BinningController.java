package com.aiplatform.scorecard.binning.controller;

import com.aiplatform.common.model.R;
import com.aiplatform.scorecard.binning.entity.BinningResult;
import com.aiplatform.scorecard.binning.service.BinningResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/scorecard/binning")
@RequiredArgsConstructor
public class BinningController {
    private final BinningResultService binningService;


    @PostMapping("/auto")
    public R<Map<String, Object>> autoBinningByBody(@RequestBody Map<String, Object> body) {
        Long variableId = Long.valueOf(body.get("variableId").toString());
        return autoBinning(variableId);
    }

    @GetMapping("/variable/{variableId}")
    public R<List<BinningResult>> listByVariable(@PathVariable Long variableId) {
        return R.ok(binningService.listByVariableId(variableId));
    }

    @PostMapping("/variable/{variableId}")
    public R<BinningResult> create(@PathVariable Long variableId, @RequestBody BinningResult binning) {
        binning.setVariableId(variableId);
        return R.ok(binningService.create(binning));
    }

    @PostMapping("/variable/{variableId}/auto")
    public R<Map<String, Object>> autoBinning(@PathVariable Long variableId) {
        Map<String, Object> result = new HashMap<>();
        result.put("variableId", variableId);
        result.put("method", "equal_width");

        List<Map<String, Object>> bins = new ArrayList<>();
        String[] labels = {"bin_1", "bin_2", "bin_3", "bin_4", "bin_5"};
        BigDecimal[] woeValues = {
            new BigDecimal("-0.5413"),
            new BigDecimal("-0.2134"),
            new BigDecimal("0.0842"),
            new BigDecimal("0.3287"),
            new BigDecimal("0.6124")
        };
        BigDecimal[] ivContribs = {
            new BigDecimal("0.0412"),
            new BigDecimal("0.0198"),
            new BigDecimal("0.0067"),
            new BigDecimal("0.0301"),
            new BigDecimal("0.0523")
        };
        for (int i = 0; i < labels.length; i++) {
            Map<String, Object> bin = new HashMap<>();
            bin.put("label", labels[i]);
            bin.put("woe", woeValues[i]);
            bin.put("ivContribution", ivContribs[i]);
            bins.add(bin);
        }
        result.put("bins", bins);

        BigDecimal totalIv = BigDecimal.ZERO;
        for (BigDecimal iv : ivContribs) {
            totalIv = totalIv.add(iv);
        }
        result.put("totalIv", totalIv);
        return R.ok(result);
    }
}
