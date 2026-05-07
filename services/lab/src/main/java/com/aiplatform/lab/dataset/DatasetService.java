package com.aiplatform.lab.dataset;

import com.aiplatform.lab.common.MinioService;
import com.aiplatform.lab.common.PageResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetMapper datasetMapper;
    private final DatasetVersionMapper datasetVersionMapper;
    private final MinioService minioService;

    public Dataset create(Dataset dataset) {
        datasetMapper.insert(dataset);
        return dataset;
    }

    public Dataset update(Dataset dataset) {
        datasetMapper.updateById(dataset);
        return datasetMapper.selectById(dataset.getId());
    }

    public void delete(Long id) {
        Dataset ds = datasetMapper.selectById(id);
        if (ds != null && ds.getStoragePath() != null) {
            minioService.delete(ds.getStoragePath());
        }
        datasetMapper.deleteById(id);
        datasetVersionMapper.delete(new LambdaQueryWrapper<DatasetVersion>()
                .eq(DatasetVersion::getDatasetId, id));
    }

    public Dataset getById(Long id) {
        return datasetMapper.selectById(id);
    }

    public PageResult<Dataset> list(Long tenantId, Long projectId, int page, int size) {
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Dataset::getTenantId, tenantId);
        if (projectId != null) wrapper.eq(Dataset::getProjectId, projectId);
        wrapper.orderByDesc(Dataset::getCreatedAt);
        IPage<Dataset> result = datasetMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Dataset upload(Long tenantId, Long projectId, String name, String description, MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String storagePath = String.format("%s/%s/datasets/%s%s", tenantId, projectId, UUID.randomUUID(), extension);

            minioService.upload(storagePath, file.getInputStream(), file.getSize(), file.getContentType());

            Dataset dataset = new Dataset();
            dataset.setTenantId(tenantId);
            dataset.setProjectId(projectId);
            dataset.setName(name);
            dataset.setDescription(description);
            dataset.setSizeBytes(file.getSize());
            dataset.setStoragePath(storagePath);
            dataset.setVersion(1);

            if (".csv".equalsIgnoreCase(extension)) {
                detectCsvSchema(dataset, file);
            }

            datasetMapper.insert(dataset);
            createVersion(dataset);

            return dataset;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload dataset: " + e.getMessage(), e);
        }
    }

    private void detectCsvSchema(Dataset dataset, MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            JSONArray schemaArray = new JSONArray();
            long rowCount = 0;

            Iterator<CSVRecord> it = parser.iterator();
            if (it.hasNext()) {
                CSVRecord firstRow = it.next();
                rowCount++;
                for (int i = 0; i < headers.size(); i++) {
                    String value = i < firstRow.size() ? firstRow.get(i) : "";
                    JSONObject col = new JSONObject();
                    col.put("name", headers.get(i));
                    col.put("type", inferType(value));
                    schemaArray.add(col);
                }
                while (it.hasNext()) {
                    it.next();
                    rowCount++;
                }
            } else {
                for (String header : headers) {
                    JSONObject col = new JSONObject();
                    col.put("name", header);
                    col.put("type", "STRING");
                    schemaArray.add(col);
                }
            }

            dataset.setSchemaJson(schemaArray.toJSONString());
            dataset.setRowCount(rowCount);

        } catch (Exception e) {
            log.warn("Failed to detect CSV schema: {}", e.getMessage());
        }
    }

    private String inferType(String value) {
        if (value == null || value.trim().isEmpty()) return "STRING";
        try {
            Double.parseDouble(value);
            if (value.contains(".")) return "DOUBLE";
            return "INTEGER";
        } catch (NumberFormatException e) {
            return "STRING";
        }
    }

    private void createVersion(Dataset dataset) {
        long count = datasetVersionMapper.selectCount(
                new LambdaQueryWrapper<DatasetVersion>().eq(DatasetVersion::getDatasetId, dataset.getId()));
        DatasetVersion version = new DatasetVersion();
        version.setTenantId(dataset.getTenantId());
        version.setDatasetId(dataset.getId());
        version.setVersion(dataset.getVersion() != null ? dataset.getVersion() : (int)(count + 1));
        version.setStoragePath(dataset.getStoragePath());
        version.setChangeLog("Initial version");
        datasetVersionMapper.insert(version);
    }

    public PageResult<Map<String, String>> preview(Long id, int page, int size) {
        Dataset dataset = datasetMapper.selectById(id);
        if (dataset == null || dataset.getStoragePath() == null) {
            return PageResult.of(Collections.emptyList(), 0, page, size);
        }

        try (InputStream is = minioService.download(dataset.getStoragePath());
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            List<Map<String, String>> rows = new ArrayList<>();
            int skip = page * size;
            int count = 0;
            int collected = 0;

            for (CSVRecord record : parser) {
                if (count < skip) { count++; continue; }
                if (collected >= size) break;
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : headers) {
                    row.put(header, record.isSet(headers.indexOf(header)) ? record.get(header) : "");
                }
                rows.add(row);
                collected++;
                count++;
            }

            long total = dataset.getRowCount() != null ? dataset.getRowCount() : count;
            return PageResult.of(rows, total, page, size);

        } catch (Exception e) {
            throw new RuntimeException("Failed to preview dataset: " + e.getMessage(), e);
        }
    }

    public List<DatasetVersion> listVersions(Long datasetId) {
        return datasetVersionMapper.selectList(
                new LambdaQueryWrapper<DatasetVersion>()
                        .eq(DatasetVersion::getDatasetId, datasetId)
                        .orderByDesc(DatasetVersion::getVersion));
    }

    public DatasetVersion getVersion(Long datasetId, Integer version) {
        return datasetVersionMapper.selectOne(
                new LambdaQueryWrapper<DatasetVersion>()
                        .eq(DatasetVersion::getDatasetId, datasetId)
                        .eq(DatasetVersion::getVersion, version));
    }
}
