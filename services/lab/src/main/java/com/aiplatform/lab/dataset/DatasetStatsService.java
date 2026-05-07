package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.MinioService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetStatsService {

    private final DatasetMapper datasetMapper;
    private final DatasetStatMapper datasetStatMapper;
    private final MinioService minioService;

    public List<DatasetStat> computeStats(Long datasetId, int version) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null || dataset.getSchemaJson() == null) {
            return Collections.emptyList();
        }

        List<String[]> allRows = loadCsvData(dataset);
        JSONArray schema = JSON.parseArray(dataset.getSchemaJson());

        Map<String, Integer> colIndex = new HashMap<>();
        List<String> numericColumns = new ArrayList<>();
        List<String> categoricalColumns = new ArrayList<>();

        for (int i = 0; i < schema.size(); i++) {
            JSONObject col = schema.getJSONObject(i);
            String name = col.getString("name");
            String type = col.getString("type");
            colIndex.put(name, i);
            if ("INTEGER".equals(type) || "DOUBLE".equals(type)) {
                numericColumns.add(name);
            } else {
                categoricalColumns.add(name);
            }
        }

        datasetStatMapper.delete(new LambdaQueryWrapper<DatasetStat>()
                .eq(DatasetStat::getDatasetId, datasetId));

        JSONObject columnStats = new JSONObject();
        for (String col : numericColumns) {
            columnStats.put(col, computeNumericColumnStats(allRows, colIndex.get(col)));
        }
        for (String col : categoricalColumns) {
            columnStats.put(col, computeCategoricalColumnStats(allRows, colIndex.get(col)));
        }

        JSONObject correlationJson = null;
        if (numericColumns.size() >= 2) {
            correlationJson = computeCorrelation(numericColumns, allRows, colIndex);
        }

        DatasetStat stat = new DatasetStat();
        stat.setDatasetId(datasetId);
        stat.setColumnStatsJson(columnStats.toJSONString());
        if (correlationJson != null) {
            stat.setCorrelationJson(correlationJson.toJSONString());
        }
        datasetStatMapper.insert(stat);

        return Collections.singletonList(stat);
    }

    private JSONObject computeNumericColumnStats(List<String[]> rows, int colIdx) {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        long missing = 0;
        for (String[] row : rows) {
            if (colIdx >= row.length || row[colIdx] == null || row[colIdx].trim().isEmpty()) {
                missing++;
                continue;
            }
            try { ds.addValue(Double.parseDouble(row[colIdx].trim())); }
            catch (NumberFormatException e) { missing++; }
        }
        JSONObject stats = new JSONObject();
        stats.put("count", ds.getN());
        stats.put("missing", missing);
        stats.put("missingRate", rows.isEmpty() ? 0.0 : (double) missing / rows.size());
        if (ds.getN() > 0) {
            stats.put("min", ds.getMin());
            stats.put("max", ds.getMax());
            stats.put("mean", ds.getMean());
            stats.put("std", ds.getStandardDeviation());
            stats.put("median", ds.getPercentile(50));
        }
        return stats;
    }

    private JSONObject computeCategoricalColumnStats(List<String[]> rows, int colIdx) {
        Map<String, Long> valueCounts = new LinkedHashMap<>();
        long missing = 0;
        for (String[] row : rows) {
            if (colIdx >= row.length || row[colIdx] == null || row[colIdx].trim().isEmpty()) {
                missing++;
                continue;
            }
            valueCounts.merge(row[colIdx].trim(), 1L, Long::sum);
        }
        List<Map<String, Object>> topValues = valueCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(e -> Map.<String, Object>of("value", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
        JSONObject stats = new JSONObject();
        stats.put("totalRows", rows.size());
        stats.put("missing", missing);
        stats.put("cardinality", valueCounts.size());
        stats.put("topValues", topValues);
        return stats;
    }

    private JSONObject computeCorrelation(List<String> cols, List<String[]> rows, Map<String, Integer> colIndex) {
        int n = cols.size();
        double[][] matrix = new double[n][n];
        PearsonsCorrelation pc = new PearsonsCorrelation();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) { matrix[i][j] = 1.0; continue; }
                int idxI = colIndex.get(cols.get(i));
                int idxJ = colIndex.get(cols.get(j));
                List<Double> xv = new ArrayList<>(), yv = new ArrayList<>();
                for (String[] row : rows) {
                    try {
                        double x = Double.parseDouble(row[idxI].trim());
                        double y = Double.parseDouble(row[idxJ].trim());
                        xv.add(x); yv.add(y);
                    } catch (Exception ignored) {}
                }
                matrix[i][j] = xv.size() >= 3 ?
                        pc.correlation(xv.stream().mapToDouble(Double::doubleValue).toArray(),
                                yv.stream().mapToDouble(Double::doubleValue).toArray()) : Double.NaN;
            }
        }
        JSONObject result = new JSONObject();
        result.put("columns", cols);
        result.put("matrix", matrix);
        return result;
    }

    private List<String[]> loadCsvData(Dataset dataset) {
        try (InputStream is = minioService.download(dataset.getStoragePath());
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            List<String[]> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) row[i] = record.get(i);
                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV data: " + e.getMessage(), e);
        }
    }

    public List<DatasetStat> getStats(Long datasetId, Integer version) {
        return datasetStatMapper.selectList(
                new LambdaQueryWrapper<DatasetStat>()
                        .eq(DatasetStat::getDatasetId, datasetId));
    }
}
