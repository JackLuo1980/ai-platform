package com.aiplatform.operation.cluster;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Cluster register(Cluster cluster) {
        cluster.setStatus("ACTIVE");
        clusterMapper.insert(cluster);
        return cluster;
    }

    public PageResult<Cluster> list(int page, int size, String status) {
        Page<Cluster> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Cluster> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Cluster::getStatus, status);
        }
        wrapper.orderByDesc(Cluster::getCreatedAt);
        Page<Cluster> result = clusterMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Cluster getStatus(Long id) {
        Cluster cluster = clusterMapper.selectById(id);
        if (cluster != null && cluster.getApiServerUrl() != null) {
            try {
                cluster.setNodeInfoJson(fetchNodeInfo(cluster));
            } catch (Exception e) {
                cluster.setStatus("UNREACHABLE");
            }
        }
        return cluster;
    }

    public Map<String, Object> getNodes(Long id) {
        Cluster cluster = clusterMapper.selectById(id);
        Map<String, Object> result = new HashMap<>();
        if (cluster == null || cluster.getApiServerUrl() == null) {
            result.put("nodes", java.util.List.of());
            return result;
        }
        try {
            String nodeInfo = fetchNodeInfo(cluster);
            JsonNode jsonNode = objectMapper.readTree(nodeInfo);
            result.put("nodes", objectMapper.convertValue(jsonNode, java.util.List.class));
        } catch (Exception e) {
            result.put("nodes", java.util.List.of());
            result.put("error", e.getMessage());
        }
        return result;
    }

    private String fetchNodeInfo(Cluster cluster) {
        RestTemplate restTemplate = new RestTemplate();
        String url = cluster.getApiServerUrl() + "/api/v1/nodes";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "{}";
        }
    }
}
