package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.MinioService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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

    public List<DatasetStat> computeStats(String datasetId, int version) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null || dataset.getSchema() == null) {
            return Collections.emptyList();
        }

        List<String[]> allRows = loadCsvData(dataset);
        JSONArray schema = JSON.parseArray(dataset.getSchema());

        List<String> numericColumns = new ArrayList<>();
        List<String> categoricalColumns = new ArrayList<>();
        Map<String, Integer> colIndex = new HashMap<>();

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

        datasetStatMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DatasetStat>()
                .eq(DatasetStat::getDatasetId, datasetId)
                .eq(DatasetStat::getVersion, version));

        List<DatasetStat> results = new ArrayList<>();

        for (String col : numericColumns) {
            DatasetStat stat = computeNumericStats(datasetId, version, col, allRows, colIndex.get(col));
            results.add(stat);
        }

        for (String col : categoricalColumns) {
            DatasetStat stat = computeCategoricalStats(datasetId, version, col, allRows, colIndex.get(col));
            results.add(stat);
        }

        if (numericColumns.size() >= 2) {
            computeCorrelationMatrix(datasetId, version, numericColumns, allRows, colIndex);
        }

        return results;
    }

    private List<String[]> loadCsvData(Dataset dataset) {
        try (InputStream is = minioService.download(dataset.getStoragePath());
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            List<String[]> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                rows.add(row);
            }
            return rows;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV data: " + e.getMessage(), e);
        }
    }

    private DatasetStat computeNumericStats(String datasetId, int version, String colName,
                                            List<String[]> rows, int colIdx) {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        long missing = 0;

        for (String[] row : rows) {
            if (colIdx >= row.length || row[colIdx] == null || row[colIdx].trim().isEmpty()) {
                missing++;
                continue;
            }
            try {
                ds.addValue(Double.parseDouble(row[colIdx].trim()));
            } catch (NumberFormatException ignored) {
                missing++;
            }
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
            stats.put("q1", ds.getPercentile(25));
            stats.put("q3", ds.getPercentile(75));
        }

        DatasetStat stat = new DatasetStat();
        stat.setDatasetId(datasetId);
        stat.setVersion(version);
        stat.setColumnName(colName);
        stat.setColumnType("NUMERIC");
        stat.setStatsJson(stats.toJSONString());
        datasetStatMapper.insert(stat);
        return stat;
    }

    private DatasetStat computeCategoricalStats(String datasetId, int version, String colName,
                                                List<String[]> rows, int colIdx) {
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
        stats.put("missingRate", rows.isEmpty() ? 0.0 : (double) missing / rows.size());
        stats.put("cardinality", valueCounts.size());
        stats.put("topValues", topValues);

        DatasetStat stat = new DatasetStat();
        stat.setDatasetId(datasetId);
        stat.setVersion(version);
        stat.setColumnName(colName);
        stat.setColumnType("CATEGORICAL");
        stat.setStatsJson(stats.toJSONString());
        datasetStatMapper.insert(stat);
        return stat;
    }

    private void computeCorrelationMatrix(String datasetId, int version,
                                          List<String> numericCols, List<String[]> rows,
                                          Map<String, Integer> colIndex) {
        Map<String, double[]> colData = new LinkedHashMap<>();
        for (String col : numericCols) {
            double[] data = new double[rows.size()];
            int idx = colIndex.get(col);
            for (int i = 0; i < rows.size(); i++) {
                try {
                    data[i] = (idx < rows.get(i).length && rows.get(i)[idx] != null && !rows.get(i)[idx].trim().isEmpty())
                            ? Double.parseDouble(rows.get(i)[idx].trim()) : Double.NaN;
                } catch (NumberFormatException e) {
                    data[i] = Double.NaN;
                }
            }
            colData.put(col, data);
        }

        int n = numericCols.size();
        double[][] matrix = new double[n][n];
        PearsonsCorrelation pc = new PearsonsCorrelation();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) { matrix[i][j] = 1.0; continue; }
                double[] x = colData.get(numericCols.get(i));
                double[] y = colData.get(numericCols.get(j));
                List<Double> xv = new ArrayList<>();
                List<Double> yv = new ArrayList<>();
                for (int k = 0; k < x.length; k++) {
                    if (!Double.isNaN(x[k]) && !Double.isNaN(y[k])) {
                        xv.add(x[k]);
                        yv.add(y[k]);
                    }
                }
                if (xv.size() < 3) { matrix[i][j] = Double.NaN; continue; }
                double[] xa = xv.stream().mapToDouble(Double::doubleValue).toArray();
                double[] ya = yv.stream().mapToDouble(Double::doubleValue).toArray();
                matrix[i][j] = pc.correlation(xa, ya);
            }
        }

        JSONObject corrStats = new JSONObject();
        corrStats.put("columns", numericCols);
        corrStats.put("matrix", matrix);

        DatasetStat stat = new DatasetStat();
        stat.setDatasetId(datasetId);
        stat.setVersion(version);
        stat.setColumnName("__correlation__");
        stat.setColumnType("CORRELATION");
        stat.setStatsJson(corrStats.toJSONString());
        datasetStatMapper.insert(stat);
    }

    public List<DatasetStat> getStats(String datasetId, Integer version) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DatasetStat> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DatasetStat>()
                        .eq(DatasetStat::getDatasetId, datasetId);
        if (version != null) {
            wrapper.eq(DatasetStat::getVersion, version);
        }
        return datasetStatMapper.selectList(wrapper);
    }
}
