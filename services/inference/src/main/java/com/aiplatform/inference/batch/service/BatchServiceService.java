package com.aiplatform.inference.batch.service;

import com.aiplatform.inference.batch.entity.BatchService;
import com.aiplatform.inference.batch.mapper.BatchServiceMapper;
import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.mapper.InferenceModelMapper;
import com.aiplatform.inference.shared.K8sClient;
import com.aiplatform.inference.shared.MinioService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceService {

    private final BatchServiceMapper batchServiceMapper;
    private final InferenceModelMapper modelMapper;
    private final K8sClient k8sClient;
    private final MinioService minioService;

    public BatchService create(String name, Long modelId, String inputPath, String config) {
        InferenceModel model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("Model not found: " + modelId);
        }

        BatchService batchService = new BatchService();
        batchService.setName(name);
        batchService.setModelId(modelId);
        batchService.setModelName(model.getName());
        batchService.setModelVersion(model.getVersion());
        batchService.setStatus("CREATED");
        batchService.setInputPath(inputPath);
        batchService.setOutputPath("batch-output/" + name + "/results.csv");
        batchService.setConfig(config);
        batchService.setTotalRecords(0);
        batchService.setProcessedRecords(0);
        batchService.setFailedRecords(0);
        batchServiceMapper.insert(batchService);

        return batchService;
    }

    public BatchService getById(Long id) {
        return batchServiceMapper.selectById(id);
    }

    public BatchService start(Long id) {
        BatchService batchService = batchServiceMapper.selectById(id);
        if (batchService == null) {
            throw new RuntimeException("Batch service not found");
        }
        if (!"CREATED".equals(batchService.getStatus()) && !"STOPPED".equals(batchService.getStatus())) {
            throw new RuntimeException("Batch service is not in a startable state");
        }

        String jobName = "batch-" + batchService.getName().toLowerCase().replaceAll("[^a-z0-9]", "-");
        Map<String, Object> jobSpec = buildJobSpec(jobName, batchService.getModelId(),
                batchService.getInputPath(), batchService.getOutputPath());
        k8sClient.createJob(jobSpec);

        batchService.setStatus("RUNNING");
        batchService.setK8sJobName(jobName);
        batchServiceMapper.updateById(batchService);

        return batchService;
    }

    public BatchService stop(Long id) {
        BatchService batchService = batchServiceMapper.selectById(id);
        if (batchService == null) {
            throw new RuntimeException("Batch service not found");
        }

        if (batchService.getK8sJobName() != null) {
            k8sClient.deleteJob(batchService.getK8sJobName());
        }

        batchService.setStatus("STOPPED");
        batchServiceMapper.updateById(batchService);

        return batchService;
    }

    public String getResultsDownloadUrl(Long id) {
        BatchService batchService = batchServiceMapper.selectById(id);
        if (batchService == null) {
            throw new RuntimeException("Batch service not found");
        }
        return minioService.getPresignedUrl(batchService.getOutputPath(), 3600);
    }

    public PageResult<BatchService> listServices(int page, int size) {
        Page<BatchService> pageParam = new Page<>(page + 1, size);
        Page<BatchService> result = batchServiceMapper.selectPage(pageParam,
                new LambdaQueryWrapper<BatchService>().orderByDesc(BatchService::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    private Map<String, Object> buildJobSpec(String name, Long modelId, String inputPath, String outputPath) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("apiVersion", "batch/v1");
        spec.put("kind", "Job");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        spec.put("metadata", metadata);
        Map<String, Object> specInner = new HashMap<>();
        specInner.put("backoffLimit", 3);
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> container = new HashMap<>();
        container.put("name", name);
        container.put("image", "ai-platform/batch-inference:latest");
        container.put("env", List.of(
                Map.of("name", "MODEL_ID", "value", modelId.toString()),
                Map.of("name", "INPUT_PATH", "value", inputPath),
                Map.of("name", "OUTPUT_PATH", "value", outputPath)
        ));
        template.put("spec", Map.of(
                "containers", List.of(container),
                "restartPolicy", "OnFailure"
        ));
        specInner.put("template", template);
        spec.put("spec", specInner);
        return spec;
    }
}
