package com.aiplatform.inference.monitor.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.monitor.entity.ServiceLog;
import com.aiplatform.inference.monitor.mapper.ServiceLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceLogService {

    private final ServiceLogMapper logMapper;

    public ServiceLog record(ServiceLog serviceLog) {
        logMapper.insert(serviceLog);
        return serviceLog;
    }

    public PageResult<ServiceLog> queryByServiceId(Long serviceId, int page, int size) {
        Page<ServiceLog> pageParam = new Page<>(page + 1, size);
        Page<ServiceLog> result = logMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ServiceLog>()
                        .eq(ServiceLog::getServiceId, serviceId)
                        .orderByDesc(ServiceLog::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public PageResult<ServiceLog> queryAll(int page, int size) {
        Page<ServiceLog> pageParam = new Page<>(page + 1, size);
        Page<ServiceLog> result = logMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ServiceLog>().orderByDesc(ServiceLog::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
}
