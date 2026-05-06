package com.aiplatform.inference.online.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.mapper.InferenceModelMapper;
import com.aiplatform.inference.online.entity.OnlineService;
import com.aiplatform.inference.online.mapper.OnlineServiceMapper;
import com.aiplatform.inference.shared.K8sClient;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.shared.ModelLoader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineServiceService {

    private final OnlineServiceMapper onlineServiceMapper;
    private final InferenceModelMapper modelMapper;
    private final K8sClient k8sClient;
    private final MinioService minioService;
    private final ModelLoader modelLoader;
    private final ObjectMapper objectMapper;

    public OnlineService deploy(String name, Long modelId, Integer replicas, BigDecimal cpuCores, Integer memoryMb, String releaseType) {
        InferenceModel model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("Model not found: " + modelId);
        }
        if (!"APPROVED".equals(model.getStatus())) {
            throw new RuntimeException("Model is not approved");
        }

        byte[] modelData = minioService.downloadFileBytes(model.getFilePath());
        modelLoader.loadModel(model.getFilePath(), modelData);

        String deploymentName = "inference-" + name.toLowerCase().replaceAll("[^a-z0-9]", "-");
        String serviceName = deploymentName + "-svc";

        Map<String, Object> deployment = buildDeploymentSpec(deploymentName, model.getFilePath(), replicas, cpuCores, memoryMb);
        k8sClient.createDeployment(deployment);

        Map<String, Object> service = buildServiceSpec(serviceName, deploymentName, 8080);
        k8sClient.createService(service);

        OnlineService onlineService = new OnlineService();
        onlineService.setName(name);
        onlineService.setModelId(modelId);
        onlineService.setModelName(model.getName());
        onlineService.setModelVersion(model.getVersion());
        onlineService.setModelPath(model.getFilePath());
        onlineService.setStatus("RUNNING");
        onlineService.setReplicas(replicas != null ? replicas : 1);
        onlineService.setCpuCores(cpuCores != null ? cpuCores : new BigDecimal("1"));
        onlineService.setMemoryMb(memoryMb != null ? memoryMb : 1024);
        onlineService.setPort(8080);
        onlineService.setReleaseType(releaseType != null ? releaseType : "canary");
        onlineService.setK8sDeploymentName(deploymentName);
        onlineService.setK8sServiceName(serviceName);
        onlineService.setEndpoint("/api/v1/inference/online/" + deploymentName + "/predict");
        onlineServiceMapper.insert(onlineService);

        return onlineService;
    }

    public OnlineService getById(Long id) {
        return onlineServiceMapper.selectById(id);
    }

    public OnlineService updateConfig(Long id, Integer replicas, BigDecimal cpuCores, Integer memoryMb) {
        OnlineService service = onlineServiceMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }

        if (replicas != null) service.setReplicas(replicas);
        if (cpuCores != null) service.setCpuCores(cpuCores);
        if (memoryMb != null) service.setMemoryMb(memoryMb);

        Map<String, Object> deployment = buildDeploymentSpec(
                service.getK8sDeploymentName(),
                service.getModelPath(),
                service.getReplicas(),
                service.getCpuCores(),
                service.getMemoryMb()
        );
        k8sClient.updateDeployment(service.getK8sDeploymentName(), deployment);
        onlineServiceMapper.updateById(service);

        return service;
    }

    public void stop(Long id) {
        OnlineService service = onlineServiceMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }

        k8sClient.deleteDeployment(service.getK8sDeploymentName());
        k8sClient.deleteService(service.getK8sServiceName());
        modelLoader.unloadModel(service.getModelPath());

        service.setStatus("STOPPED");
        onlineServiceMapper.updateById(service);
    }

    public void toggleReleaseType(Long id, String releaseType) {
        OnlineService service = onlineServiceMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("Online service not found");
        }
        service.setReleaseType(releaseType);
        onlineServiceMapper.updateById(service);
    }

    public PageResult<OnlineService> listServices(int page, int size) {
        Page<OnlineService> pageParam = new Page<>(page + 1, size);
        Page<OnlineService> result = onlineServiceMapper.selectPage(pageParam,
                new LambdaQueryWrapper<OnlineService>().orderByDesc(OnlineService::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    private Map<String, Object> buildDeploymentSpec(String name, String modelPath, Integer replicas, BigDecimal cpu, Integer mem) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("apiVersion", "apps/v1");
        spec.put("kind", "Deployment");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        spec.put("metadata", metadata);
        Map<String, Object> specInner = new HashMap<>();
        specInner.put("replicas", replicas != null ? replicas : 1);
        Map<String, Object> selector = new HashMap<>();
        selector.put("matchLabels", Map.of("app", name));
        specInner.put("selector", selector);
        Map<String, Object> template = new HashMap<>();
        template.put("metadata", Map.of("labels", Map.of("app", name)));
        Map<String, Object> container = new HashMap<>();
        container.put("name", name);
        container.put("image", "ai-platform/inference-server:latest");
        container.put("ports", List.of(Map.of("containerPort", 8080)));
        container.put("env", List.of(Map.of("name", "MODEL_PATH", "value", modelPath)));
        Map<String, Object> resources = new HashMap<>();
        resources.put("requests", Map.of("cpu", (cpu != null ? cpu : "1") + "", "memory", (mem != null ? mem : 1024) + "Mi"));
        resources.put("limits", Map.of("cpu", (cpu != null ? cpu : "1") + "", "memory", (mem != null ? mem : 1024) + "Mi"));
        container.put("resources", resources);
        template.put("spec", Map.of("containers", List.of(container)));
        specInner.put("template", template);
        spec.put("spec", specInner);
        return spec;
    }

    private Map<String, Object> buildServiceSpec(String name, String deploymentName, int port) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("apiVersion", "v1");
        spec.put("kind", "Service");
        spec.put("metadata", Map.of("name", name));
        Map<String, Object> specInner = new HashMap<>();
        specInner.put("selector", Map.of("app", deploymentName));
        specInner.put("ports", List.of(Map.of("port", port, "targetPort", port)));
        specInner.put("type", "ClusterIP");
        spec.put("spec", specInner);
        return spec;
    }
}
