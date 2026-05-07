package com.aiplatform.scorecard.rule.service;

import com.aiplatform.scorecard.rule.entity.ScoringRule;
import com.aiplatform.scorecard.rule.mapper.ScoringRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringRuleService {
    private final ScoringRuleMapper ruleMapper;

    public List<ScoringRule> listByModelId(Long modelId) {
        LambdaQueryWrapper<ScoringRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoringRule::getModelId, modelId);
        return ruleMapper.selectList(wrapper);
    }

    public ScoringRule create(ScoringRule entity) {
        ruleMapper.insert(entity);
        return entity;
    }

    public ScoringRule update(Long id, ScoringRule entity) {
        entity.setId(id);
        ruleMapper.updateById(entity);
        return entity;
    }
}
