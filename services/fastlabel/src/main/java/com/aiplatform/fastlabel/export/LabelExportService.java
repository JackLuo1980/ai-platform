package com.aiplatform.fastlabel.export;

import com.aiplatform.fastlabel.client.LabApiClient;
import com.aiplatform.fastlabel.client.MinioService;
import com.aiplatform.fastlabel.dataset.LabelDataset;
import com.aiplatform.fastlabel.dataset.LabelDatasetMapper;
import com.aiplatform.fastlabel.item.LabelItem;
import com.aiplatform.fastlabel.item.LabelItemMapper;
import com.aiplatform.fastlabel.task.LabelTask;
import com.aiplatform.fastlabel.task.LabelTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelExportService {

    private final LabelExportMapper exportMapper;
    private final LabelTaskMapper taskMapper;
    private final LabelDatasetMapper datasetMapper;
    private final LabelItemMapper itemMapper;
    private final MinioService minioService;
    private final LabApiClient labApiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    @SneakyThrows
    public LabelExport createExport(Long taskId, String format, String exportedBy) {
        LabelTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }

        LabelDataset dataset = datasetMapper.selectById(task.getDatasetId());

        LabelExport export = new LabelExport();
        export.setTaskId(taskId);
        export.setDatasetId(task.getDatasetId());
        export.setName("export_" + task.getName() + "_" + System.currentTimeMillis());
        export.setFormat(format);
        export.setExportedBy(exportedBy);
        export.setStatus("PROCESSING");
        exportMapper.insert(export);

        try {
            String content;
            String contentType;
            String extension;

            switch (format) {
                case "COCO" -> {
                    content = generateCocoJson(task, dataset);
                    contentType = "application/json";
                    extension = ".json";
                }
                case "JSON" -> {
                    content = generateJsonExport(task, dataset);
                    contentType = "application/json";
                    extension = ".json";
                }
                case "CSV" -> {
                    content = generateCsvExport(task, dataset);
                    contentType = "text/csv";
                    extension = ".csv";
                }
                default -> throw new RuntimeException("Unsupported export format: " + format);
            }

            String objectName = "exports/" + export.getId() + "/" + UUID.randomUUID() + extension;
            minioService.upload(objectName, content.getBytes(StandardCharsets.UTF_8), contentType);

            LambdaQueryWrapper<LabelItem> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(LabelItem::getTaskId, taskId);
            countWrapper.isNotNull(LabelItem::getAnnotationJson);
            long itemCount = itemMapper.selectCount(countWrapper);

            export.setFilePath(objectName);
            export.setItemCount((int) itemCount);
            export.setStatus("COMPLETED");
            exportMapper.updateById(export);
        } catch (Exception e) {
            export.setStatus("FAILED");
            export.setErrorMessage(e.getMessage());
            exportMapper.updateById(export);
            throw e;
        }

        return export;
    }

    public LabelExport getById(Long id) {
        return exportMapper.selectById(id);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Map<String, Object> pushToLab(Long exportId) {
        LabelExport export = exportMapper.selectById(exportId);
        if (export == null) {
            throw new RuntimeException("Export not found");
        }
        if (!"COMPLETED".equals(export.getStatus())) {
            throw new RuntimeException("Export is not completed");
        }

        LabelDataset dataset = datasetMapper.selectById(export.getDatasetId());
        String datasetName = dataset != null ? dataset.getName() : "Exported Dataset";
        String datasetType = dataset != null ? dataset.getType() : "IMAGE_CLASSIFY";

        Map<String, Object> createResult = labApiClient.createDataset(
                datasetName + " (Labeled)", datasetType, "Pushed from FastLabel");

        if (createResult.containsKey("error")) {
            throw new RuntimeException("Failed to create lab dataset: " + createResult.get("error"));
        }

        Long labDatasetId = null;
        Object dataObj = createResult.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object idObj = dataMap.get("id");
            if (idObj instanceof Number num) {
                labDatasetId = num.longValue();
            }
        }

        if (labDatasetId == null) {
            throw new RuntimeException("Failed to get lab dataset ID");
        }

        String exportContent;
        try (var stream = minioService.download(export.getFilePath())) {
            exportContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, Object> pushResult = labApiClient.pushData(labDatasetId, exportContent);

        return Map.of(
                "exportId", exportId,
                "labDatasetId", labDatasetId,
                "pushResult", pushResult
        );
    }

    @SneakyThrows
    private String generateCocoJson(LabelTask task, LabelDataset dataset) {
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, task.getId());
        wrapper.isNotNull(LabelItem::getAnnotationJson);
        List<LabelItem> items = itemMapper.selectList(wrapper);

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode info = objectMapper.createObjectNode();
        info.put("description", task.getName());
        info.put("version", "1.0");
        root.set("info", info);

        ArrayNode images = objectMapper.createArrayNode();
        ArrayNode annotations = objectMapper.createArrayNode();

        long annotationId = 1;
        for (LabelItem item : items) {
            ObjectNode imageNode = objectMapper.createObjectNode();
            imageNode.put("id", item.getId());
            imageNode.put("file_name",
                    item.getDataPath() != null ? item.getDataPath() : "item_" + item.getId());
            images.add(imageNode);

            if (item.getAnnotationJson() != null) {
                JsonNode annotation = objectMapper.readTree(item.getAnnotationJson());

                if (annotation.has("boxes")) {
                    JsonNode boxes = annotation.get("boxes");
                    for (JsonNode box : boxes) {
                        ObjectNode annNode = objectMapper.createObjectNode();
                        annNode.put("id", annotationId++);
                        annNode.put("image_id", item.getId());
                        ArrayNode bbox = objectMapper.createArrayNode();
                        bbox.add(box.get("x").asInt());
                        bbox.add(box.get("y").asInt());
                        bbox.add(box.get("w").asInt());
                        bbox.add(box.get("h").asInt());
                        annNode.set("bbox", bbox);
                        annNode.put("area", box.get("w").asInt() * box.get("h").asInt());
                        annNode.put("category", box.get("label").asText());
                        annNode.put("iscrowd", 0);
                        annNode.set("segmentation", objectMapper.createArrayNode());
                        annotations.add(annNode);
                    }
                } else if (annotation.has("label")) {
                    ObjectNode annNode = objectMapper.createObjectNode();
                    annNode.put("id", annotationId++);
                    annNode.put("image_id", item.getId());
                    annNode.put("category", annotation.get("label").asText());
                    annNode.set("segmentation", objectMapper.createArrayNode());
                    annNode.put("area", 0);
                    annNode.put("iscrowd", 0);
                    annotations.add(annNode);
                } else if (annotation.has("mask_points")) {
                    ObjectNode annNode = objectMapper.createObjectNode();
                    annNode.put("id", annotationId++);
                    annNode.put("image_id", item.getId());
                    annNode.put("category", annotation.has("label") ? annotation.get("label").asText() : "object");
                    annNode.set("segmentation", annotation.get("mask_points"));
                    annNode.put("area", 0);
                    annNode.put("iscrowd", 0);
                    annotations.add(annNode);
                }
            }
        }

        root.set("images", images);
        root.set("annotations", annotations);

        ArrayNode categories = objectMapper.createArrayNode();
        ObjectNode catNode = objectMapper.createObjectNode();
        catNode.put("id", 1);
        catNode.put("name", "object");
        categories.add(catNode);
        root.set("categories", categories);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    @SneakyThrows
    private String generateJsonExport(LabelTask task, LabelDataset dataset) {
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, task.getId());
        wrapper.isNotNull(LabelItem::getAnnotationJson);
        List<LabelItem> items = itemMapper.selectList(wrapper);

        ArrayNode root = objectMapper.createArrayNode();
        for (LabelItem item : items) {
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("id", item.getId());
            entry.put("data", item.getDataContent() != null ? item.getDataContent() : "");
            entry.put("dataPath", item.getDataPath() != null ? item.getDataPath() : "");
            if (item.getAnnotationJson() != null) {
                entry.set("annotation", objectMapper.readTree(item.getAnnotationJson()));
            }
            root.add(entry);
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    @SneakyThrows
    private String generateCsvExport(LabelTask task, LabelDataset dataset) {
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, task.getId());
        wrapper.isNotNull(LabelItem::getAnnotationJson);
        List<LabelItem> items = itemMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder();
        sb.append("id,data,label\n");

        for (LabelItem item : items) {
            String data = item.getDataContent() != null ? item.getDataContent() : "";
            data = escapeCsv(data);

            String label = "";
            if (item.getAnnotationJson() != null) {
                JsonNode ann = objectMapper.readTree(item.getAnnotationJson());
                if (ann.has("label")) {
                    label = ann.get("label").asText();
                }
            }
            sb.append(item.getId()).append(",").append(data).append(",").append(escapeCsv(label)).append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
