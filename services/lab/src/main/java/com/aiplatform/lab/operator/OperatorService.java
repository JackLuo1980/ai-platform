package com.aiplatform.lab.operator;

import com.aiplatform.lab.common.PageResult;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorMapper operatorMapper;
    private final OperatorVersionMapper operatorVersionMapper;

    private static final Map<String, JSONObject> PRESET_OPERATORS = new LinkedHashMap<>();

    static {
        PRESET_OPERATORS.put("filter", new JSONObject(Map.of(
                "name", "Filter", "category", "TRANSFORM",
                "description", "Filter rows by condition",
                "params", Map.of("column", "", "operator", "==", "value", ""))));
        PRESET_OPERATORS.put("select", new JSONObject(Map.of(
                "name", "Select", "category", "TRANSFORM",
                "description", "Select specific columns",
                "params", Map.of("columns", List.of()))));
        PRESET_OPERATORS.put("cast", new JSONObject(Map.of(
                "name", "Cast", "category", "TRANSFORM",
                "description", "Change column data type",
                "params", Map.of("column", "", "targetType", "STRING"))));
        PRESET_OPERATORS.put("missing_value", new JSONObject(Map.of(
                "name", "Missing Value", "category", "TRANSFORM",
                "description", "Handle missing values",
                "params", Map.of("column", "", "strategy", "DROP", "fillValue", ""))));
        PRESET_OPERATORS.put("aggregate", new JSONObject(Map.of(
                "name", "Aggregate", "category", "TRANSFORM",
                "description", "Aggregate data by groups",
                "params", Map.of("groupBy", List.of(), "aggregations", Map.of()))));
        PRESET_OPERATORS.put("join", new JSONObject(Map.of(
                "name", "Join", "category", "TRANSFORM",
                "description", "Join two datasets",
                "params", Map.of("rightDataset", "", "joinType", "INNER", "leftKey", "", "rightKey", ""))));
        PRESET_OPERATORS.put("split", new JSONObject(Map.of(
                "name", "Split", "category", "TRANSFORM",
                "description", "Split dataset into train/test",
                "params", Map.of("trainRatio", 0.8, "randomSeed", 42, "strategy", "RANDOM"))));
        PRESET_OPERATORS.put("sample", new JSONObject(Map.of(
                "name", "Sample", "category", "TRANSFORM",
                "description", "Sample rows from dataset",
                "params", Map.of("fraction", 0.1, "randomSeed", 42, "strategy", "RANDOM"))));
        PRESET_OPERATORS.put("scale", new JSONObject(Map.of(
                "name", "Scale", "category", "TRANSFORM",
                "description", "Scale numeric features",
                "params", Map.of("columns", List.of(), "method", "STANDARD", "min", 0, "max", 1))));
        PRESET_OPERATORS.put("onehot", new JSONObject(Map.of(
                "name", "One-Hot Encoding", "category", "TRANSFORM",
                "description", "Encode categorical columns as one-hot",
                "params", Map.of("columns", List.of(), "dropFirst", false))));
    }

    public Operator create(Operator operator) {
        if ("PRESET".equals(operator.getType())) {
            JSONObject preset = PRESET_OPERATORS.get(operator.getName().toLowerCase());
            if (preset != null) {
                operator.setParamsSchemaJson(preset.toJSONString());
                operator.setCategory(preset.getString("category"));
                operator.setDescription(preset.getString("description"));
            }
        }
        operatorMapper.insert(operator);
        createVersion(operator);
        return operator;
    }

    public Operator update(Operator operator) {
        operatorMapper.updateById(operator);
        createVersion(operator);
        return operatorMapper.selectById(operator.getId());
    }

    public void delete(Long id) {
        operatorMapper.deleteById(id);
        operatorVersionMapper.delete(new LambdaQueryWrapper<OperatorVersion>()
                .eq(OperatorVersion::getOperatorId, id));
    }

    public Operator getById(Long id) {
        return operatorMapper.selectById(id);
    }

    public PageResult<Operator> list(Long tenantId, String category, String type, int page, int size) {
        LambdaQueryWrapper<Operator> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(Operator::getTenantId, tenantId);
        if (category != null) wrapper.eq(Operator::getCategory, category);
        if (type != null) wrapper.eq(Operator::getType, type);
        wrapper.orderByDesc(Operator::getCreatedAt);
        IPage<Operator> result = operatorMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public List<OperatorVersion> listVersions(Long operatorId) {
        return operatorVersionMapper.selectList(
                new LambdaQueryWrapper<OperatorVersion>()
                        .eq(OperatorVersion::getOperatorId, operatorId)
                        .orderByDesc(OperatorVersion::getVersion));
    }

    public Map<String, Object> test(Long id, Map<String, Object> testParams) {
        Operator op = operatorMapper.selectById(id);
        if (op == null) {
            return Map.of("success", false, "message", "Operator not found");
        }
        return Map.of("success", true, "message", "Operator config valid",
                "operator", op.getName(), "params", testParams);
    }

    private void createVersion(Operator operator) {
        Long count = operatorVersionMapper.selectCount(
                new LambdaQueryWrapper<OperatorVersion>()
                        .eq(OperatorVersion::getOperatorId, operator.getId()));
        OperatorVersion v = new OperatorVersion();
        v.setTenantId(operator.getTenantId());
        v.setOperatorId(operator.getId());
        v.setVersion(count.intValue() + 1);
        v.setCode(operator.getCode());
        v.setParamsSchemaJson(operator.getParamsSchemaJson());
        operatorVersionMapper.insert(v);
    }

    public List<Map<String, Object>> listPresets() {
        List<Map<String, Object>> presets = new ArrayList<>();
        PRESET_OPERATORS.forEach((key, value) -> {
            Map<String, Object> entry = new LinkedHashMap<>(value);
            entry.put("key", key);
            presets.add(entry);
        });
        return presets;
    }
}
