package com.aiplatform.scorecard.variable.service;

import com.aiplatform.scorecard.variable.entity.ScVariable;
import com.aiplatform.scorecard.variable.mapper.ScVariableMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScVariableService {
    private final ScVariableMapper variableMapper;

    public List<ScVariable> list(Long projectId) {
        LambdaQueryWrapper<ScVariable> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ScVariable::getProjectId, projectId);
        }
        wrapper.orderByDesc(ScVariable::getCreatedAt);
        return variableMapper.selectList(wrapper);
    }

    public ScVariable getById(Long id) {
        return variableMapper.selectById(id);
    }

    public ScVariable create(ScVariable entity) {
        variableMapper.insert(entity);
        return entity;
    }

    public ScVariable update(Long id, ScVariable entity) {
        entity.setId(id);
        variableMapper.updateById(entity);
        return entity;
    }

    public void delete(Long id) {
        variableMapper.deleteById(id);
    }
}
