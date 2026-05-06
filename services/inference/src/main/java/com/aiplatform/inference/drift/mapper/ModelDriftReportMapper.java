package com.aiplatform.inference.drift.mapper;

import com.aiplatform.inference.drift.entity.ModelDriftReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ModelDriftReportMapper extends BaseMapper<ModelDriftReport> {
}
