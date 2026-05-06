package com.aiplatform.inference.monitor.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.monitor.entity.ServiceMetrics;
import com.aiplatform.inference.monitor.mapper.ServiceMetricsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceMetricsService {

    private final ServiceMetricsMapper metricsMapper;

    public ServiceMetrics record(ServiceMetrics metrics) {
        metricsMapper.insert(metrics);
        return metrics;
    }

    public PageResult<ServiceMetrics> queryByServiceId(Long serviceId, int page, int size) {
        Page<ServiceMetrics> pageParam = new Page<>(page + 1, size);
        Page<ServiceMetrics> result = metricsMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ServiceMetrics>()
                        .eq(ServiceMetrics::getServiceId, serviceId)
                        .orderByDesc(ServiceMetrics::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public PageResult<ServiceMetrics> queryAll(int page, int size) {
        Page<ServiceMetrics> pageParam = new Page<>(page + 1, size);
        Page<ServiceMetrics> result = metricsMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ServiceMetrics>().orderByDesc(ServiceMetrics::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
}
