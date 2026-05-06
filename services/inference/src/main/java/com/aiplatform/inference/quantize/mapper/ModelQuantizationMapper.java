package com.aiplatform.inference.quantize.mapper;

import com.aiplatform.inference.quantize.entity.ModelQuantization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ModelQuantizationMapper extends BaseMapper<ModelQuantization> {
}
