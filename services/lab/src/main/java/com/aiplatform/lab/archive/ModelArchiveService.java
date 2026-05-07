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
        if (archive.getApprovalStatus() == null) {
            archive.setApprovalStatus("PENDING");
        }
        modelArchiveMapper.insert(archive);
        return archive;
    }

    public ModelArchive update(ModelArchive archive) {
        modelArchiveMapper.updateById(archive);
        return modelArchiveMapper.selectById(archive.getId());
    }

    public void delete(Long id) {
        modelArchiveMapper.deleteById(id);
        modelFileMapper.delete(new LambdaQueryWrapper<ModelFile>()
                .eq(ModelFile::getArchiveId, id));
    }

    public ModelArchive getById(Long id) {
        return modelArchiveMapper.selectById(id);
    }

    public PageResult<ModelArchive> list(Long tenantId, String approvalStatus, int page, int size) {
        LambdaQueryWrapper<ModelArchive> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) wrapper.eq(ModelArchive::getTenantId, tenantId);
        if (approvalStatus != null) wrapper.eq(ModelArchive::getApprovalStatus, approvalStatus);
        wrapper.orderByDesc(ModelArchive::getCreatedAt);
        IPage<ModelArchive> result = modelArchiveMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public ModelArchive archiveFromExperiment(Long tenantId, Long experimentRunId,
                                              String name, String format, String runtimeImage,
                                              String evaluationSummaryJson, List<ModelFile> files) {
        ModelArchive archive = new ModelArchive();
        archive.setTenantId(tenantId);
        archive.setName(name);
        archive.setFormat(format);
        archive.setRuntimeImage(runtimeImage);
        archive.setSourceType("EXPERIMENT");
        archive.setSourceExperimentId(experimentRunId);
        archive.setEvaluationSummaryJson(evaluationSummaryJson);
        archive.setApprovalStatus("PENDING");
        archive.setDeleted(0);
        modelArchiveMapper.insert(archive);

        if (files != null) {
            for (ModelFile file : files) {
                file.setTenantId(tenantId);
                file.setArchiveId(archive.getId());
                file.setDeleted(0);
                modelFileMapper.insert(file);
            }
        }
        return archive;
    }

    public ModelArchive submitApproval(Long id) {
        ModelArchive archive = modelArchiveMapper.selectById(id);
        if (archive == null) throw new RuntimeException("Model archive not found");
        archive.setApprovalStatus("PENDING");
        modelArchiveMapper.updateById(archive);
        return archive;
    }

    public ModelArchive approve(Long id, boolean approved, String comment) {
        ModelArchive archive = modelArchiveMapper.selectById(id);
        if (archive == null) throw new RuntimeException("Model archive not found");
        archive.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
        modelArchiveMapper.updateById(archive);
        return archive;
    }

    public List<ModelFile> listFiles(Long archiveId) {
        return modelFileMapper.selectList(
                new LambdaQueryWrapper<ModelFile>()
                        .eq(ModelFile::getArchiveId, archiveId));
    }
}
