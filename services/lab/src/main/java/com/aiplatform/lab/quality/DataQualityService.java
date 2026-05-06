package com.aiplatform.lab.quality;

import com.aiplatform.lab.common.MinioService;
import com.aiplatform.lab.dataset.DatasetStat;
import com.aiplatform.lab.dataset.DatasetStatsService;
import com.aiplatform.lab.dataset.DatasetMapper;
import com.aiplatform.lab.dataset.Dataset;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataQualityService {

    private final DatasetMapper datasetMapper;
    private final DatasetStatsService datasetStatsService;
    private final MinioService minioService;

    public Map<String, Object> validate(String datasetId, int version, Map<String, Object> rules) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            return Map.of("datasetId", datasetId, "valid", false, "errors", List.of("Dataset not found"));
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (dataset.getSchema() != null && rules.containsKey("expectedColumns")) {
            @SuppressWarnings("unchecked")
            List<String> expected = (List<String>) rules.get("expectedColumns");
            JSONArray schema = JSON.parseArray(dataset.getSchema());
            Set<String> actualCols = new HashSet<>();
            for (int i = 0; i < schema.size(); i++) {
                actualCols.add(schema.getJSONObject(i).getString("name"));
            }
            for (String col : expected) {
                if (!actualCols.contains(col)) {
                    errors.add("Missing required column: " + col);
                }
            }
        }

        if (rules.containsKey("expectedTypes")) {
            @SuppressWarnings("unchecked")
            Map<String, String> expectedTypes = (Map<String, String>) rules.get("expectedTypes");
            if (dataset.getSchema() != null) {
                JSONArray schema = JSON.parseArray(dataset.getSchema());
                for (int i = 0; i < schema.size(); i++) {
                    JSONObject col = schema.getJSONObject(i);
                    String name = col.getString("name");
                    String type = col.getString("type");
                    if (expectedTypes.containsKey(name) && !expectedTypes.get(name).equals(type)) {
                        warnings.add("Column '" + name + "' type mismatch: expected " + expectedTypes.get(name) + ", got " + type);
                    }
                }
            }
        }

        if (rules.containsKey("maxMissingRate")) {
            double maxRate = ((Number) rules.get("maxMissingRate")).doubleValue();
            List<DatasetStat> stats = datasetStatsService.getStats(datasetId, version);
            for (DatasetStat stat : stats) {
                if ("NUMERIC".equals(stat.getColumnType()) || "CATEGORICAL".equals(stat.getColumnType())) {
                    JSONObject statJson = JSON.parseObject(stat.getStatsJson());
                    double missingRate = statJson.getDoubleValue("missingRate");
                    if (missingRate > maxRate) {
                        errors.add(String.format("Column '%s' missing rate %.2f exceeds threshold %.2f",
                                stat.getColumnName(), missingRate, maxRate));
                    }
                }
            }
        }

        if (rules.containsKey("nonNullColumns")) {
            @SuppressWarnings("unchecked")
            List<String> nonNullCols = (List<String>) rules.get("nonNullColumns");
            List<DatasetStat> stats = datasetStatsService.getStats(datasetId, version);
            for (DatasetStat stat : stats) {
                if (nonNullCols.contains(stat.getColumnName())) {
                    JSONObject statJson = JSON.parseObject(stat.getStatsJson());
                    double missingRate = statJson.getDoubleValue("missingRate");
                    if (missingRate > 0) {
                        errors.add("Column '" + stat.getColumnName() + "' has null values but non-null constraint specified");
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

    public Map<String, Object> score(String datasetId, int version) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            return Map.of("datasetId", datasetId, "score", 0, "details", Map.of("error", "Dataset not found"));
        }

        List<DatasetStat> stats = datasetStatsService.getStats(datasetId, version);
        if (stats.isEmpty()) {
            stats = datasetStatsService.computeStats(datasetId, version);
        }

        double completenessScore = 100.0;
        double schemaScore = 100.0;
        double distributionScore = 100.0;
        Map<String, Object> details = new LinkedHashMap<>();

        int columnCount = 0;
        double totalMissingRate = 0;
        List<String> issues = new ArrayList<>();

        for (DatasetStat stat : stats) {
            if (!"NUMERIC".equals(stat.getColumnType()) && !"CATEGORICAL".equals(stat.getColumnType())) {
                continue;
            }
            columnCount++;
            JSONObject statJson = JSON.parseObject(stat.getStatsJson());
            double missingRate = statJson.getDoubleValue("missingRate");
            totalMissingRate += missingRate;

            if (missingRate > 0.5) {
                issues.add("Column '" + stat.getColumnName() + "' has >50% missing values");
                completenessScore -= 10;
            } else if (missingRate > 0.2) {
                issues.add("Column '" + stat.getColumnName() + "' has >20% missing values");
                completenessScore -= 5;
            }
        }

        if (columnCount > 0) {
            double avgMissing = totalMissingRate / columnCount;
            completenessScore = Math.max(0, completenessScore - (avgMissing * 50));
        }

        if (dataset.getSchema() == null || dataset.getSchema().isEmpty()) {
            schemaScore = 50;
            issues.add("No schema detected");
        }

        if (dataset.getRowCount() == null || dataset.getRowCount() < 10) {
            distributionScore -= 20;
            issues.add("Insufficient data rows for reliable distribution analysis");
        }

        Map<String, Object> distributionDetails = checkDistribution(datasetId, version, stats);
        if (distributionDetails.containsKey("warnings")) {
            @SuppressWarnings("unchecked")
            List<String> distWarnings = (List<String>) distributionDetails.get("warnings");
            distributionScore -= distWarnings.size() * 5;
            issues.addAll(distWarnings);
        }

        completenessScore = Math.max(0, Math.min(100, completenessScore));
        schemaScore = Math.max(0, Math.min(100, schemaScore));
        distributionScore = Math.max(0, Math.min(100, distributionScore));

        double overallScore = completenessScore * 0.4 + schemaScore * 0.3 + distributionScore * 0.3;
        overallScore = Math.round(overallScore * 100.0) / 100.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("datasetId", datasetId);
        result.put("version", version);
        result.put("score", overallScore);
        details.put("completeness", completenessScore);
        details.put("schema", schemaScore);
        details.put("distribution", distributionScore);
        details.put("issues", issues);
        details.put("columnCount", columnCount);
        result.put("details", details);

        return result;
    }

    private Map<String, Object> checkDistribution(String datasetId, int version, List<DatasetStat> stats) {
        List<String> warnings = new ArrayList<>();
        for (DatasetStat stat : stats) {
            if ("CATEGORICAL".equals(stat.getColumnType())) {
                JSONObject statJson = JSON.parseObject(stat.getStatsJson());
                int cardinality = statJson.getIntValue("cardinality");
                if (cardinality == 1) {
                    warnings.add("Column '" + stat.getColumnName() + "' has only one unique value (constant)");
                }
            }
        }
        return Map.of("warnings", warnings);
    }

    private Map<String, Object> chiSquareTest(double[] expected, long[] observed) {
        try {
            ChiSquareTest test = new ChiSquareTest();
            double pValue = test.chiSquareTest(expected, observed);
            boolean significant = test.chiSquareTest(expected, observed, 0.05);
            return Map.of("pValue", pValue, "significant", significant, "alpha", 0.05);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
