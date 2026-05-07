package com.aiplatform.scorecard.model.service;

import com.aiplatform.scorecard.model.entity.ScModel;
import com.aiplatform.scorecard.model.mapper.ScModelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScModelService {
    private final ScModelMapper modelMapper;

    public List<ScModel> list(Long projectId) {
        LambdaQueryWrapper<ScModel> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ScModel::getProjectId, projectId);
        }
        wrapper.orderByDesc(ScModel::getCreatedAt);
        return modelMapper.selectList(wrapper);
    }

    public ScModel getById(Long id) {
        return modelMapper.selectById(id);
    }

    public ScModel create(ScModel entity) {
        entity.setStatus("DRAFT");
        modelMapper.insert(entity);
        return entity;
    }

    public ScModel update(Long id, ScModel entity) {
        entity.setId(id);
        modelMapper.updateById(entity);
        return entity;
    }

    public void delete(Long id) {
        modelMapper.deleteById(id);
    }
}
