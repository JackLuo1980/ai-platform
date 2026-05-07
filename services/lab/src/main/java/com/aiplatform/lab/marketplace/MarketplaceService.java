package com.aiplatform.lab.marketplace;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.archive.ModelArchive;
import com.aiplatform.lab.archive.ModelArchiveMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketplaceModelMapper marketplaceModelMapper;
    private final ModelArchiveMapper modelArchiveMapper;

    public PageResult<MarketplaceModel> list(String taskType, String framework, String search, int page, int size) {
        LambdaQueryWrapper<MarketplaceModel> wrapper = new LambdaQueryWrapper<>();
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(MarketplaceModel::getTaskType, taskType);
        }
        if (framework != null && !framework.isEmpty()) {
            wrapper.eq(MarketplaceModel::getFramework, framework);
        }
        if (search != null && !search.isEmpty()) {
            wrapper.and(w -> w.like(MarketplaceModel::getName, search)
                    .or().like(MarketplaceModel::getDescription, search));
        }
        wrapper.orderByDesc(MarketplaceModel::getCreatedAt);
        IPage<MarketplaceModel> result = marketplaceModelMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public MarketplaceModel getById(Long id) {
        return marketplaceModelMapper.selectById(id);
    }

    @Transactional
    public ModelArchive addToLab(Long modelId, Long tenantId, String name, String version) {
        MarketplaceModel model = marketplaceModelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("Marketplace model not found: " + modelId);
        }

        ModelArchive archive = new ModelArchive();
        archive.setTenantId(tenantId);
        archive.setProjectId(model.getProjectId());
        archive.setName(name != null ? name : model.getName());
        archive.setFormat(version != null ? version : "1.0.0");
        archive.setSourceType("IMPORT");
        archive.setApprovalStatus("PENDING");
        archive.setDeleted(0);
        modelArchiveMapper.insert(archive);

        return archive;
    }

    public java.util.List<MarketplaceModel> listByTask(String taskType) {
        return marketplaceModelMapper.selectList(
                new LambdaQueryWrapper<MarketplaceModel>()
                        .eq(MarketplaceModel::getTaskType, taskType)
                        .orderByDesc(MarketplaceModel::getCreatedAt));
    }
}
