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

    public void deleteGroup(Long id) {
        featureGroupMapper.deleteById(id);
        featureDefinitionMapper.delete(new LambdaQueryWrapper<FeatureDefinition>()
                .eq(FeatureDefinition::getGroupId, id));
        featureValueOfflineMapper.delete(new LambdaQueryWrapper<FeatureValueOffline>()
                .eq(FeatureValueOffline::getGroupId, id));
        deleteOnlineValues(id);
    }

    public FeatureGroup getGroup(Long id) {
        return featureGroupMapper.selectById(id);
    }

    public PageResult<FeatureGroup> listGroups(Long tenantId, int page, int size) {
        LambdaQueryWrapper<FeatureGroup> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(FeatureGroup::getTenantId, tenantId);
        wrapper.orderByDesc(FeatureGroup::getCreatedAt);
        IPage<FeatureGroup> result = featureGroupMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public List<FeatureDefinition> listDefinitions(Long groupId) {
        return featureDefinitionMapper.selectList(
                new LambdaQueryWrapper<FeatureDefinition>()
                        .eq(FeatureDefinition::getGroupId, groupId)
                        .orderByAsc(FeatureDefinition::getCreatedAt));
    }

    public FeatureDefinition addDefinition(FeatureDefinition definition) {
        featureDefinitionMapper.insert(definition);
        return definition;
    }

    public Map<String, Object> pointInTimeQuery(Long groupId, String entityKey, LocalDateTime timestamp) {
        FeatureGroup group = featureGroupMapper.selectById(groupId);
        if (group == null) return Collections.emptyMap();

        List<FeatureDefinition> definitions = featureDefinitionMapper.selectList(
                new LambdaQueryWrapper<FeatureDefinition>()
                        .eq(FeatureDefinition::getGroupId, groupId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityKeys", group.getEntityKeysJson());
        result.put("entityValue", entityKey);
        result.put("timestamp", timestamp.toString());

        Map<String, Object> features = new LinkedHashMap<>();
        for (FeatureDefinition def : definitions) {
            LambdaQueryWrapper<FeatureValueOffline> wrapper = new LambdaQueryWrapper<FeatureValueOffline>()
                    .eq(FeatureValueOffline::getGroupId, groupId)
                    .eq(FeatureValueOffline::getEntityKey, entityKey)
                    .le(FeatureValueOffline::getEventTimestamp, timestamp)
                    .orderByDesc(FeatureValueOffline::getEventTimestamp)
                    .last("LIMIT 1");
            FeatureValueOffline val = featureValueOfflineMapper.selectOne(wrapper);
            features.put(def.getName(), val != null ? val.getFeatureJson() : def.getDefaultValue());
        }
        result.put("features", features);

        return result;
    }

    public List<Map<String, Object>> batchPointInTimeQuery(Long groupId, List<String> entityKeys,
                                                           LocalDateTime timestamp) {
        return entityKeys.stream()
                .map(ek -> pointInTimeQuery(groupId, ek, timestamp))
                .collect(Collectors.toList());
    }

    @Transactional
    public FeatureJob triggerCompute(Long groupId, Long tenantId) {
        FeatureJob job = new FeatureJob();
        job.setTenantId(tenantId);
        job.setGroupId(groupId);
        job.setJobType("MANUAL");
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        featureJobMapper.insert(job);
        featureJobMapper.updateById(job);
        return job;
    }

    public Map<String, String> getFromOnline(Long tenantId, Long groupId, String entityKey) {
        String redisKey = buildRedisKey(String.valueOf(tenantId), String.valueOf(groupId), entityKey);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        Map<String, String> result = new LinkedHashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), v != null ? v.toString() : null));
        return result;
    }

    private void deleteOnlineValues(Long groupId) {
        List<FeatureValueOnline> online = featureValueOnlineMapper.selectList(
                new LambdaQueryWrapper<FeatureValueOnline>()
                        .eq(FeatureValueOnline::getGroupId, groupId));
        for (FeatureValueOnline val : online) {
            String key = buildRedisKey(String.valueOf(val.getGroupId()), String.valueOf(groupId), val.getEntityKey());
            redisTemplate.opsForHash().delete(key);
        }
        featureValueOnlineMapper.delete(new LambdaQueryWrapper<FeatureValueOnline>()
                .eq(FeatureValueOnline::getGroupId, groupId));
    }

    private String buildRedisKey(String tenantId, String groupId, String entityKey) {
        return "feature:" + tenantId + ":" + groupId + ":" + entityKey;
    }

    public PageResult<FeatureJob> listJobs(Long groupId, Long tenantId, int page, int size) {
        LambdaQueryWrapper<FeatureJob> wrapper = new LambdaQueryWrapper<>();
        if (groupId != null) wrapper.eq(FeatureJob::getGroupId, groupId);
        if (tenantId != null) wrapper.eq(FeatureJob::getTenantId, tenantId);
        wrapper.orderByDesc(FeatureJob::getCreatedAt);
        IPage<FeatureJob> result = featureJobMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public void saveOfflineValues(Long tenantId, Long groupId,
                                  List<Map<String, String>> values) {
        for (Map<String, String> entry : values) {
            FeatureValueOffline offline = new FeatureValueOffline();
            offline.setGroupId(groupId);
            offline.setEntityKey(entry.get("entityKey"));
            offline.setFeatureJson(entry.get("featureJson"));
            offline.setEventTimestamp(entry.containsKey("eventTimestamp")
                    ? LocalDateTime.parse(entry.get("eventTimestamp")) : LocalDateTime.now());
            offline.setCreatedAt(LocalDateTime.now());
            featureValueOfflineMapper.insert(offline);
        }
    }

    public List<FeatureDefinition> listAllDefinitions() {
        return featureDefinitionMapper.selectList(null);
    }
}
