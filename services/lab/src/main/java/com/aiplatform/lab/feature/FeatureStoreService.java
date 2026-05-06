package com.aiplatform.lab.feature;

import com.aiplatform.lab.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureStoreService {

    private final FeatureGroupMapper featureGroupMapper;
    private final FeatureDefinitionMapper featureDefinitionMapper;
    private final FeatureValueOfflineMapper featureValueOfflineMapper;
    private final FeatureValueOnlineMapper featureValueOnlineMapper;
    private final FeatureJobMapper featureJobMapper;
    private final StringRedisTemplate redisTemplate;

    public FeatureGroup createGroup(FeatureGroup group) {
        featureGroupMapper.insert(group);
        return group;
    }

    public FeatureGroup updateGroup(FeatureGroup group) {
        featureGroupMapper.updateById(group);
        return featureGroupMapper.selectById(group.getId());
    }

    public void deleteGroup(String id) {
        featureGroupMapper.deleteById(id);
        featureDefinitionMapper.delete(new LambdaQueryWrapper<FeatureDefinition>()
                .eq(FeatureDefinition::getGroupId, id));
        featureValueOfflineMapper.delete(new LambdaQueryWrapper<FeatureValueOffline>()
                .eq(FeatureValueOffline::getGroupId, id));
        deleteOnlineValues(id);
    }

    public FeatureGroup getGroup(String id) {
        return featureGroupMapper.selectById(id);
    }

    public PageResult<FeatureGroup> listGroups(String tenantId, int page, int size) {
        LambdaQueryWrapper<FeatureGroup> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(FeatureGroup::getTenantId, tenantId);
        wrapper.orderByDesc(FeatureGroup::getCreatedAt);
        IPage<FeatureGroup> result = featureGroupMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public List<FeatureDefinition> listDefinitions(String groupId) {
        return featureDefinitionMapper.selectList(
                new LambdaQueryWrapper<FeatureDefinition>()
                        .eq(FeatureDefinition::getGroupId, groupId)
                        .orderByAsc(FeatureDefinition::getCreatedAt));
    }

    public FeatureDefinition addDefinition(FeatureDefinition definition) {
        featureDefinitionMapper.insert(definition);
        return definition;
    }

    public Map<String, Object> pointInTimeQuery(String groupId, String entityKeyValue, LocalDateTime timestamp) {
        FeatureGroup group = featureGroupMapper.selectById(groupId);
        if (group == null) return Collections.emptyMap();

        List<FeatureDefinition> definitions = featureDefinitionMapper.selectList(
                new LambdaQueryWrapper<FeatureDefinition>()
                        .eq(FeatureDefinition::getGroupId, groupId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityKey", group.getEntityKey());
        result.put("entityValue", entityKeyValue);
        result.put("timestamp", timestamp.toString());

        Map<String, Object> features = new LinkedHashMap<>();
        for (FeatureDefinition def : definitions) {
            LambdaQueryWrapper<FeatureValueOffline> wrapper = new LambdaQueryWrapper<FeatureValueOffline>()
                    .eq(FeatureValueOffline::getGroupId, groupId)
                    .eq(FeatureValueOffline::getDefinitionId, def.getId())
                    .eq(FeatureValueOffline::getEntityKeyValue, entityKeyValue)
                    .le(FeatureValueOffline::getEventTime, timestamp)
                    .orderByDesc(FeatureValueOffline::getEventTime)
                    .last("LIMIT 1");
            FeatureValueOffline val = featureValueOfflineMapper.selectOne(wrapper);
            features.put(def.getName(), val != null ? val.getValue() : def.getDefaultValue());
        }
        result.put("features", features);

        return result;
    }

    public List<Map<String, Object>> batchPointInTimeQuery(String groupId, List<String> entityKeyValues,
                                                           LocalDateTime timestamp) {
        return entityKeyValues.stream()
                .map(ek -> pointInTimeQuery(groupId, ek, timestamp))
                .collect(Collectors.toList());
    }

    @Transactional
    public FeatureJob triggerCompute(String groupId, String tenantId) {
        FeatureJob job = new FeatureJob();
        job.setTenantId(tenantId);
        job.setGroupId(groupId);
        job.setType("MANUAL");
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        featureJobMapper.insert(job);

        try {
            List<FeatureDefinition> definitions = featureDefinitionMapper.selectList(
                    new LambdaQueryWrapper<FeatureDefinition>()
                            .eq(FeatureDefinition::getGroupId, groupId));

            for (FeatureDefinition def : definitions) {
                computeFeatureValues(groupId, def, tenantId);
            }

            job.setStatus("SUCCESS");
        } catch (Exception e) {
            job.setStatus("FAILED");
            log.error("Feature compute failed for group {}: {}", groupId, e.getMessage());
        }

        job.setFinishedAt(LocalDateTime.now());
        featureJobMapper.updateById(job);
        return job;
    }

    private void computeFeatureValues(String groupId, FeatureDefinition def, String tenantId) {
        List<FeatureValueOffline> existing = featureValueOfflineMapper.selectList(
                new LambdaQueryWrapper<FeatureValueOffline>()
                        .eq(FeatureValueOffline::getGroupId, groupId)
                        .eq(FeatureValueOffline::getDefinitionId, def.getId()));

        for (FeatureValueOffline val : existing) {
            syncToOnline(tenantId, groupId, def.getId(), val.getEntityKeyValue(), val.getValue());
        }
    }

    public void syncToOnline(String tenantId, String groupId, String definitionId,
                             String entityKeyValue, String value) {
        String redisKey = buildRedisKey(tenantId, groupId, entityKeyValue);
        String field = definitionId;
        redisTemplate.opsForHash().put(redisKey, field, value);
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

        FeatureValueOnline online = new FeatureValueOnline();
        online.setTenantId(tenantId);
        online.setGroupId(groupId);
        online.setDefinitionId(definitionId);
        online.setEntityKeyValue(entityKeyValue);
        online.setValue(value);
        online.setUpdatedAt(LocalDateTime.now());
        featureValueOnlineMapper.insert(online);
    }

    public Map<String, String> getFromOnline(String tenantId, String groupId, String entityKeyValue) {
        String redisKey = buildRedisKey(tenantId, groupId, entityKeyValue);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        Map<String, String> result = new LinkedHashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), v != null ? v.toString() : null));
        return result;
    }

    public void syncAllToOnline(String groupId, String tenantId) {
        List<FeatureValueOffline> values = featureValueOfflineMapper.selectList(
                new LambdaQueryWrapper<FeatureValueOffline>()
                        .eq(FeatureValueOffline::getGroupId, groupId)
                        .eq(FeatureValueOffline::getTenantId, tenantId));

        for (FeatureValueOffline val : values) {
            syncToOnline(tenantId, groupId, val.getDefinitionId(),
                    val.getEntityKeyValue(), val.getValue());
        }
    }

    private void deleteOnlineValues(String groupId) {
        List<FeatureValueOnline> online = featureValueOnlineMapper.selectList(
                new LambdaQueryWrapper<FeatureValueOnline>()
                        .eq(FeatureValueOnline::getGroupId, groupId));
        for (FeatureValueOnline val : online) {
            String key = buildRedisKey(val.getTenantId(), groupId, val.getEntityKeyValue());
            redisTemplate.opsForHash().delete(key, val.getDefinitionId());
        }
        featureValueOnlineMapper.delete(new LambdaQueryWrapper<FeatureValueOnline>()
                .eq(FeatureValueOnline::getGroupId, groupId));
    }

    private String buildRedisKey(String tenantId, String groupId, String entityKeyValue) {
        return "feature:" + tenantId + ":" + groupId + ":" + entityKeyValue;
    }

    public PageResult<FeatureJob> listJobs(String groupId, String tenantId, int page, int size) {
        LambdaQueryWrapper<FeatureJob> wrapper = new LambdaQueryWrapper<>();
        if (groupId != null) wrapper.eq(FeatureJob::getGroupId, groupId);
        if (tenantId != null) wrapper.eq(FeatureJob::getTenantId, tenantId);
        wrapper.orderByDesc(FeatureJob::getCreatedAt);
        IPage<FeatureJob> result = featureJobMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public void saveOfflineValues(String tenantId, String groupId, String definitionId,
                                  List<Map<String, String>> values) {
        for (Map<String, String> entry : values) {
            FeatureValueOffline offline = new FeatureValueOffline();
            offline.setTenantId(tenantId);
            offline.setGroupId(groupId);
            offline.setDefinitionId(definitionId);
            offline.setEntityKeyValue(entry.get("entityKey"));
            offline.setValue(entry.get("value"));
            offline.setEventTime(entry.containsKey("eventTime")
                    ? LocalDateTime.parse(entry.get("eventTime")) : LocalDateTime.now());
            offline.setCreatedAt(LocalDateTime.now());
            featureValueOfflineMapper.insert(offline);
        }
    }
}
