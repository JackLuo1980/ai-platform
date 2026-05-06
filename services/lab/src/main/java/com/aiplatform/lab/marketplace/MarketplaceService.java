package com.aiplatform.lab.marketplace;

import com.aiplatform.lab.common.PageResult;
import com.aiplatform.lab.archive.ModelArchive;
import com.aiplatform.lab.archive.ModelArchiveMapper;
import com.aiplatform.lab.archive.ModelFile;
import com.aiplatform.lab.archive.ModelFileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketplaceModelMapper marketplaceModelMapper;
    private final ModelArchiveMapper modelArchiveMapper;
    private final ModelFileMapper modelFileMapper;

    public PageResult<MarketplaceModel> list(String task, String framework, String search, int page, int size) {
        LambdaQueryWrapper<MarketplaceModel> wrapper = new LambdaQueryWrapper<>();
        if (task != null && !task.isEmpty()) {
            wrapper.eq(MarketplaceModel::getTask, task);
        }
        if (framework != null && !framework.isEmpty()) {
            wrapper.eq(MarketplaceModel::getFramework, framework);
        }
        if (search != null && !search.isEmpty()) {
            wrapper.and(w -> w.like(MarketplaceModel::getName, search)
                    .or().like(MarketplaceModel::getDescription, search)
                    .or().like(MarketplaceModel::getTags, search));
        }
        wrapper.orderByDesc(MarketplaceModel::getCreatedAt);
        IPage<MarketplaceModel> result = marketplaceModelMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public MarketplaceModel getById(String id) {
        return marketplaceModelMapper.selectById(id);
    }

    @Transactional
    public ModelArchive addToLab(String modelId, String tenantId, String name, String version) {
        MarketplaceModel model = marketplaceModelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("Marketplace model not found: " + modelId);
        }

        ModelArchive archive = new ModelArchive();
        archive.setTenantId(tenantId);
        archive.setName(name != null ? name : model.getName());
        archive.setVersion(version != null ? version : "1.0.0");
        archive.setSource("IMPORT");
        archive.setSourceId(modelId);
        archive.setDescription(model.getDescription());
        archive.setFramework(model.getFramework());
        archive.setStatus("DRAFT");
        modelArchiveMapper.insert(archive);

        return archive;
    }

    public List<MarketplaceModel> listByTask(String task) {
        return marketplaceModelMapper.selectList(
                new LambdaQueryWrapper<MarketplaceModel>()
                        .eq(MarketplaceModel::getTask, task)
                        .orderByDesc(MarketplaceModel::getCreatedAt));
    }
}
