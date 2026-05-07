package com.aiplatform.scorecard.binning.service;

import com.aiplatform.scorecard.binning.entity.BinningResult;
import com.aiplatform.scorecard.binning.mapper.BinningResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BinningResultService {
    private final BinningResultMapper binningMapper;

    public List<BinningResult> listByVariableId(Long variableId) {
        LambdaQueryWrapper<BinningResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BinningResult::getVariableId, variableId);
        return binningMapper.selectList(wrapper);
    }

    public BinningResult getById(Long id) {
        return binningMapper.selectById(id);
    }

    public BinningResult create(BinningResult entity) {
        binningMapper.insert(entity);
        return entity;
    }

    public BinningResult update(Long id, BinningResult entity) {
        entity.setId(id);
        binningMapper.updateById(entity);
        return entity;
    }
}
