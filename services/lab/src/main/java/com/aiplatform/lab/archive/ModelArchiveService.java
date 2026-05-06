package com.aiplatform.lab.archive;

import com.aiplatform.lab.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ModelArchiveService {

    private final ModelArchiveMapper modelArchiveMapper;
    private final ModelFileMapper modelFileMapper;

    public ModelArchive create(ModelArchive archive) {
        if (archive.getStatus() == null) {
            archive.setStatus("DRAFT");
        }
        modelArchiveMapper.insert(archive);
        return archive;
    }

    public ModelArchive update(ModelArchive archive) {
        modelArchiveMapper.updateById(archive);
        return modelArchiveMapper.selectById(archive.getId());
    }

    public void delete(String id) {
        modelArchiveMapper.deleteById(id);
        modelFileMapper.delete(new LambdaQueryWrapper<ModelFile>()
                .eq(ModelFile::getArchiveId, id));
    }

    public ModelArchive getById(String id) {
        return modelArchiveMapper.selectById(id);
    }

    public PageResult<ModelArchive> list(String tenantId, String status, int page, int size) {
        LambdaQueryWrapper<ModelArchive> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(ModelArchive::getTenantId, tenantId);
        if (status != null) wrapper.eq(ModelArchive::getStatus, status);
        wrapper.orderByDesc(ModelArchive::getCreatedAt);
        IPage<ModelArchive> result = modelArchiveMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public ModelArchive archiveFromExperiment(String tenantId, String experimentRunId,
                                              String name, String version, String framework,
                                              String metricsJson, List<ModelFile> files) {
        ModelArchive archive = new ModelArchive();
        archive.setTenantId(tenantId);
        archive.setName(name);
        archive.setVersion(version);
        archive.setSource("EXPERIMENT");
        archive.setSourceId(experimentRunId);
        archive.setFramework(framework);
        archive.setMetrics(metricsJson);
        archive.setStatus("DRAFT");
        modelArchiveMapper.insert(archive);

        if (files != null) {
            for (ModelFile file : files) {
                file.setTenantId(tenantId);
                file.setArchiveId(archive.getId());
                modelFileMapper.insert(file);
            }
        }

        return archive;
    }

    @Transactional
    public ModelArchive importThirdParty(String tenantId, String name, String version,
                                         String framework, String description,
                                         String metricsJson, List<ModelFile> files) {
        ModelArchive archive = new ModelArchive();
        archive.setTenantId(tenantId);
        archive.setName(name);
        archive.setVersion(version);
        archive.setSource("IMPORT");
        archive.setFramework(framework);
        archive.setDescription(description);
        archive.setMetrics(metricsJson);
        archive.setStatus("DRAFT");
        modelArchiveMapper.insert(archive);

        if (files != null) {
            for (ModelFile file : files) {
                file.setTenantId(tenantId);
                file.setArchiveId(archive.getId());
                modelFileMapper.insert(file);
            }
        }

        return archive;
    }

    public ModelArchive submitApproval(String id) {
        ModelArchive archive = modelArchiveMapper.selectById(id);
        if (archive == null) throw new RuntimeException("Model archive not found");
        archive.setStatus("PENDING");
        modelArchiveMapper.updateById(archive);
        return archive;
    }

    public ModelArchive approve(String id, boolean approved, String comment) {
        ModelArchive archive = modelArchiveMapper.selectById(id);
        if (archive == null) throw new RuntimeException("Model archive not found");
        archive.setStatus(approved ? "APPROVED" : "REJECTED");
        modelArchiveMapper.updateById(archive);
        return archive;
    }

    public List<ModelFile> listFiles(String archiveId) {
        return modelFileMapper.selectList(
                new LambdaQueryWrapper<ModelFile>()
                        .eq(ModelFile::getArchiveId, archiveId));
    }
}
