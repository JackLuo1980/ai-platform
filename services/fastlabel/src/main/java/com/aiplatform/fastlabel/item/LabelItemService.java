package com.aiplatform.fastlabel.item;

import com.aiplatform.fastlabel.client.MinioService;
import com.aiplatform.fastlabel.task.LabelTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelItemService {

    private final LabelItemMapper itemMapper;
    private final LabelTaskService taskService;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    public LabelItem getById(Long id) {
        LabelItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("Item not found");
        }
        return item;
    }

    public Map<String, Object> getItemDetail(Long id) {
        LabelItem item = getById(id);
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", item.getId());
        detail.put("taskId", item.getTaskId());
        detail.put("datasetId", item.getDatasetId());
        detail.put("status", item.getStatus());
        detail.put("assignedTo", item.getAssignedTo());
        detail.put("annotation", parseAnnotation(item.getAnnotationJson()));
        detail.put("reviewComment", item.getReviewComment());
        detail.put("reviewedBy", item.getReviewedBy());
        detail.put("reviewedAt", item.getReviewedAt());

        if (item.getDataContent() != null && !item.getDataContent().isEmpty()) {
            detail.put("data", item.getDataContent());
        } else if (item.getDataPath() != null && !item.getDataPath().isEmpty()) {
            detail.put("dataUrl", minioService.getPresignedUrl(item.getDataPath()));
        }

        return detail;
    }

    @Transactional
    public LabelItem annotate(Long id, String annotationJson) {
        LabelItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("Item not found");
        }

        validateAnnotation(annotationJson);

        item.setAnnotationJson(annotationJson);
        item.setStatus("LABELED");
        itemMapper.updateById(item);

        if (item.getTaskId() != null) {
            taskService.updateTaskProgress(item.getTaskId());
        }

        return item;
    }

    @Transactional
    public LabelItem review(Long id, String action, String comment, String reviewedBy) {
        LabelItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("Item not found");
        }

        if (!"LABELED".equals(item.getStatus()) && !"REVIEWED".equals(item.getStatus())
                && !"REJECTED".equals(item.getStatus())) {
            throw new RuntimeException("Item is not in a reviewable state");
        }

        switch (action) {
            case "approve" -> {
                item.setStatus("REVIEWED");
                item.setReviewedBy(reviewedBy);
                item.setReviewComment(comment);
                item.setReviewedAt(LocalDateTime.now());
            }
            case "reject" -> {
                item.setStatus("REJECTED");
                item.setReviewedBy(reviewedBy);
                item.setReviewComment(comment);
                item.setReviewedAt(LocalDateTime.now());
            }
            default -> throw new RuntimeException("Invalid review action: " + action);
        }

        itemMapper.updateById(item);

        if (item.getTaskId() != null) {
            taskService.updateTaskProgress(item.getTaskId());
        }

        return item;
    }

    public List<Map<String, Object>> getAnnotationsByTask(Long taskId) {
        LambdaQueryWrapper<LabelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelItem::getTaskId, taskId);
        wrapper.isNotNull(LabelItem::getAnnotationJson);
        wrapper.orderByAsc(LabelItem::getCreatedAt);
        List<LabelItem> items = itemMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LabelItem item : items) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("itemId", item.getId());
            entry.put("status", item.getStatus());
            entry.put("annotation", parseAnnotation(item.getAnnotationJson()));
            entry.put("data", item.getDataContent());
            entry.put("dataPath", item.getDataPath());
            result.add(entry);
        }
        return result;
    }

    @SneakyThrows
    private Object parseAnnotation(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(json, Object.class);
    }

    @SneakyThrows
    private void validateAnnotation(String annotationJson) {
        JsonNode node = objectMapper.readTree(annotationJson);

        if (node.has("boxes")) {
            ArrayNode boxes = (ArrayNode) node.get("boxes");
            for (JsonNode box : boxes) {
                if (!box.has("x") || !box.has("y") || !box.has("w") || !box.has("h") || !box.has("label")) {
                    throw new RuntimeException("Invalid object detection annotation: missing required fields");
                }
            }
        }

        if (node.has("entities")) {
            ArrayNode entities = (ArrayNode) node.get("entities");
            for (JsonNode entity : entities) {
                if (!entity.has("start") || !entity.has("end") || !entity.has("type") || !entity.has("text")) {
                    throw new RuntimeException("Invalid NER annotation: missing required fields");
                }
            }
        }

        if (node.has("relations")) {
            ArrayNode relations = (ArrayNode) node.get("relations");
            for (JsonNode rel : relations) {
                if (!rel.has("head") || !rel.has("tail") || !rel.has("type")) {
                    throw new RuntimeException("Invalid RE annotation: missing required fields");
                }
            }
        }

        if (node.has("segments")) {
            ArrayNode segments = (ArrayNode) node.get("segments");
            for (JsonNode seg : segments) {
                if (!seg.has("start") || !seg.has("end") || !seg.has("label")) {
                    throw new RuntimeException("Invalid audio segment annotation: missing required fields (start, end, label)");
                }
                double start = seg.get("start").asDouble();
                double end = seg.get("end").asDouble();
                if (start < 0 || end < 0 || end <= start) {
                    throw new RuntimeException("Invalid audio segment: start must be >= 0 and end must be > start");
                }
            }
        }
    }

    public List<LabelItem> listItems(Long taskId, String status) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LabelItem> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(LabelItem::getTaskId, taskId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LabelItem::getStatus, status);
        }
        wrapper.orderByDesc(LabelItem::getCreatedAt);
        return itemMapper.selectList(wrapper);
    }
}
