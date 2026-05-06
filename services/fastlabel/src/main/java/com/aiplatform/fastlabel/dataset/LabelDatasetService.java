package com.aiplatform.fastlabel.dataset;

import com.aiplatform.common.model.PageResult;
import com.aiplatform.fastlabel.client.MinioService;
import com.aiplatform.fastlabel.item.LabelItem;
import com.aiplatform.fastlabel.item.LabelItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelDatasetService {

    private final LabelDatasetMapper datasetMapper;
    private final LabelItemMapper itemMapper;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LabelDataset create(LabelDataset dataset) {
        dataset.setStatus("ACTIVE");
        dataset.setItemCount(0);
        datasetMapper.insert(dataset);
        return dataset;
    }

    public PageResult<LabelDataset> list(int page, int size, String type, String keyword) {
        Page<LabelDataset> pageReq = new Page<>(page + 1, size);
        LambdaQueryWrapper<LabelDataset> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(LabelDataset::getType, type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(LabelDataset::getName, keyword);
        }
        wrapper.orderByDesc(LabelDataset::getCreatedAt);
        Page<LabelDataset> result = datasetMapper.selectPage(pageReq, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public LabelDataset getById(Long id) {
        return datasetMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        datasetMapper.deleteById(id);
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getDatasetId, id);
        itemMapper.delete(wrapper);
    }

    @Transactional
    public LabelDataset importFromLab(String name, String type, Long sourceDatasetId, String createdBy) {
        LabelDataset dataset = new LabelDataset();
        dataset.setName(name);
        dataset.setType(type);
        dataset.setSource("IMPORT");
        dataset.setSourceDatasetId(sourceDatasetId);
        dataset.setCreatedBy(createdBy);
        dataset.setStatus("ACTIVE");
        dataset.setItemCount(0);
        datasetMapper.insert(dataset);
        return dataset;
    }

    @Transactional
    @SneakyThrows
    public LabelDataset upload(String name, String type, MultipartFile file, String createdBy) {
        LabelDataset dataset = new LabelDataset();
        dataset.setName(name);
        dataset.setType(type);
        dataset.setSource("UPLOAD");
        dataset.setCreatedBy(createdBy);
        dataset.setStatus("ACTIVE");
        dataset.setItemCount(0);
        datasetMapper.insert(dataset);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && isImageFile(originalFilename)) {
            extractImageItems(dataset, file);
        } else {
            extractJsonlItems(dataset, file);
        }

        return dataset;
    }

    @SneakyThrows
    private void extractImageItems(LabelDataset dataset, MultipartFile file) {
        String objectName = "datasets/" + dataset.getId() + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioService.upload(objectName, file.getInputStream(), file.getContentType());

        LabelItem item = new LabelItem();
        item.setDatasetId(dataset.getId());
        item.setDataPath(objectName);
        item.setStatus("PENDING");
        itemMapper.insert(item);

        dataset.setItemCount(1);
        datasetMapper.updateById(dataset);
    }

    @SneakyThrows
    private void extractJsonlItems(LabelDataset dataset, MultipartFile file) {
        List<LabelItem> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String objectName = "datasets/" + dataset.getId() + "/" + UUID.randomUUID() + ".json";
                minioService.upload(objectName, line.getBytes(StandardCharsets.UTF_8), "application/json");

                JsonNode node = objectMapper.readTree(line);
                LabelItem item = new LabelItem();
                item.setDatasetId(dataset.getId());
                item.setDataPath(objectName);
                if (node.has("text")) {
                    item.setDataContent(node.get("text").asText());
                } else {
                    item.setDataContent(line);
                }
                item.setStatus("PENDING");
                items.add(item);

                if (items.size() >= 100) {
                    batchInsertItems(items);
                    items.clear();
                }
            }
        }
        if (!items.isEmpty()) {
            batchInsertItems(items);
        }
        dataset.setItemCount(dataset.getItemCount() + items.size());
        datasetMapper.updateById(dataset);
    }

    private void batchInsertItems(List<LabelItem> items) {
        for (LabelItem item : items) {
            itemMapper.insert(item);
        }
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".bmp") || lower.endsWith(".gif") || lower.endsWith(".tiff");
    }
}
