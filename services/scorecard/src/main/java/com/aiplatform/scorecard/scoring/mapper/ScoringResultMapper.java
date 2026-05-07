package com.aiplatform.scorecard.scoring.mapper;

import com.aiplatform.scorecard.scoring.entity.ScoringResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScoringResultMapper extends BaseMapper<ScoringResult> {
}
