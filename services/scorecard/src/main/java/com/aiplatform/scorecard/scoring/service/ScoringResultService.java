package com.aiplatform.scorecard.scoring.service;

import com.aiplatform.scorecard.scoring.entity.ScoringResult;
import com.aiplatform.scorecard.scoring.mapper.ScoringResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringResultService {
    private final ScoringResultMapper scoringResultMapper;

    public List<ScoringResult> listByModelId(Long modelId) {
        LambdaQueryWrapper<ScoringResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoringResult::getModelId, modelId);
        wrapper.orderByDesc(ScoringResult::getCreatedAt);
        return scoringResultMapper.selectList(wrapper);
    }

    public ScoringResult create(ScoringResult entity) {
        scoringResultMapper.insert(entity);
        return entity;
    }
}
