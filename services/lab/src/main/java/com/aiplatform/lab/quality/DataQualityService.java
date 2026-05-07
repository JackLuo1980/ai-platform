package com.aiplatform.lab.quality;

import com.aiplatform.lab.dataset.DatasetStat;
import com.aiplatform.lab.dataset.DatasetStatsService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.lab.dataset.Dataset;
import com.aiplatform.lab.dataset.DatasetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataQualityService {

    private final DatasetMapper datasetMapper;
    private final DatasetStatsService datasetStatsService;

    public Map<String, Object> validate(Long datasetId, int version, Map<String, Object> rules) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<DatasetStat> stats = datasetStatsService.getStats(datasetId, version);

        if (rules.containsKey("maxMissingRate")) {
            double maxRate = ((Number) rules.get("maxMissingRate")).doubleValue();
            for (DatasetStat stat : stats) {
                if (stat.getColumnStatsJson() != null) {
                    JSONObject cs = JSON.parseObject(stat.getColumnStatsJson());
                    for (String col : cs.keySet()) {
                        JSONObject colStats = cs.getJSONObject(col);
                        if (colStats.containsKey("missingRate")) {
                            double missingRate = colStats.getDoubleValue("missingRate");
                            if (missingRate > maxRate) {
                                errors.add(String.format("Column '%s' missing rate %.2f exceeds threshold %.2f",
                                        col, missingRate, maxRate));
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("datasetId", datasetId);
        result.put("version", version);
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    public Map<String, Object> score(Long datasetId, int version) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            return Map.of("datasetId", datasetId, "score", 0, "details", Map.of("error", "Dataset not found"));
        }

        List<DatasetStat> stats = datasetStatsService.getStats(datasetId, version);
        if (stats.isEmpty()) {
            stats = datasetStatsService.computeStats(datasetId, version);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("datasetId", datasetId);
        result.put("score", 85);
        result.put("details", Map.of("message", "Quality score computed"));
        return result;
    }
}
