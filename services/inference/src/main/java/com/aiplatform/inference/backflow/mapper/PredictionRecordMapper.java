package com.aiplatform.inference.backflow.mapper;

import com.aiplatform.inference.backflow.entity.PredictionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PredictionRecordMapper extends BaseMapper<PredictionRecord> {
}
