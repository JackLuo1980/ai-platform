package com.aiplatform.inference.model.service;

import com.aiplatform.inference.model.entity.InferenceModel;
import com.aiplatform.inference.model.mapper.InferenceModelMapper;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceModelService {

    private final InferenceModelMapper modelMapper;
    private final MinioService minioService;
    private final RestTemplate restTemplate = new RestTemplate();

    public InferenceModel importFromLab(String labApiUrl, Long modelId, String modelName, String framework) {
        String url = labApiUrl + "/api/v1/models/" + modelId + "/export";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to fetch model from Lab");
            }

            byte[] modelData = response.getBody();
            String objectName = "models/" + modelName + "/" + modelId + ".pkl";
            minioService.uploadFile(objectName, modelData, "application/octet-stream");

            InferenceModel model = new InferenceModel();
            model.setName(modelName);
            model.setVersion("1.0.0");
            model.setFramework(framework);
            model.setModelType("imported");
            model.setFilePath(objectName);
            model.setFileSize((long) modelData.length);
            model.setStatus("PENDING");
            model.setSourceType("lab");
            model.setSourceId(modelId);
            modelMapper.insert(model);

            return model;
        } catch (Exception e) {
            log.error("Failed to import model from Lab", e);
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    public InferenceModel uploadModel(MultipartFile file, String modelName, String framework, String version) {
        try {
            String objectName = "models/" + modelName + "/" + version + "/" + file.getOriginalFilename();
            minioService.uploadFile(objectName, file.getInputStream(), file.getSize(), file.getContentType());

            InferenceModel model = new InferenceModel();
            model.setName(modelName);
            model.setVersion(version != null ? version : "1.0.0");
            model.setFramework(framework != null ? framework : "unknown");
            model.setModelType("uploaded");
            model.setFilePath(objectName);
            model.setFileSize(file.getSize());
            model.setStatus("PENDING");
            model.setSourceType("upload");
            modelMapper.insert(model);

            return model;
        } catch (Exception e) {
            log.error("Failed to upload model", e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    public InferenceModel getById(Long id) {
        return modelMapper.selectById(id);
    }

    public PageResult<InferenceModel> listModels(int page, int size, String keyword) {
        Page<InferenceModel> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<InferenceModel> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(InferenceModel::getName, keyword);
        }
        wrapper.orderByDesc(InferenceModel::getCreatedAt);
        Page<InferenceModel> result = modelMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public InferenceModel auditApprove(Long id, String remark) {
        InferenceModel model = modelMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("Model not found");
        }
        model.setStatus("APPROVED");
        model.setAuditRemark(remark);
        modelMapper.updateById(model);
        return model;
    }

    public InferenceModel auditReject(Long id, String remark) {
        InferenceModel model = modelMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("Model not found");
        }
        model.setStatus("REJECTED");
        model.setAuditRemark(remark);
        modelMapper.updateById(model);
        return model;
    }

    public InferenceModel createNewVersion(Long baseModelId, MultipartFile file, String version) {
        InferenceModel base = modelMapper.selectById(baseModelId);
        if (base == null) {
            throw new RuntimeException("Base model not found");
        }
        try {
            String objectName = "models/" + base.getName() + "/" + version + "/" + file.getOriginalFilename();
            minioService.uploadFile(objectName, file.getInputStream(), file.getSize(), file.getContentType());

            InferenceModel model = new InferenceModel();
            model.setName(base.getName());
            model.setVersion(version);
            model.setFramework(base.getFramework());
            model.setModelType("versioned");
            model.setFilePath(objectName);
            model.setFileSize(file.getSize());
            model.setStatus("PENDING");
            model.setSourceType("version");
            model.setSourceId(baseModelId);
            modelMapper.insert(model);

            return model;
        } catch (Exception e) {
            log.error("Failed to create model version", e);
            throw new RuntimeException("Version creation failed: " + e.getMessage(), e);
        }
    }
}
