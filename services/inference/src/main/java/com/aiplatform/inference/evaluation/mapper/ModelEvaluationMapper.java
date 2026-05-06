package com.aiplatform.inference.evaluation.mapper;

import com.aiplatform.inference.evaluation.entity.ModelEvaluation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ModelEvaluationMapper extends BaseMapper<ModelEvaluation> {
}
