package com.aiplatform.inference.monitor.mapper;

import com.aiplatform.inference.monitor.entity.ServiceLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceLogMapper extends BaseMapper<ServiceLog> {
}
