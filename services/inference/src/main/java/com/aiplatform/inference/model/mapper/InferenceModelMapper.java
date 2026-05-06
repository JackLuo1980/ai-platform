package com.aiplatform.inference.model.mapper;

import com.aiplatform.inference.model.entity.InferenceModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InferenceModelMapper extends BaseMapper<InferenceModel> {
}
