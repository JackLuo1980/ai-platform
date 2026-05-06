package com.aiplatform.inference.monitor.controller;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.common.R;
import com.aiplatform.inference.monitor.entity.ServiceLog;
import com.aiplatform.inference.monitor.entity.ServiceMetrics;
import com.aiplatform.inference.monitor.service.ServiceLogService;
import com.aiplatform.inference.monitor.service.ServiceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference/monitor")
@RequiredArgsConstructor
public class MonitoringController {

    private final ServiceMetricsService metricsService;
    private final ServiceLogService logService;

    @GetMapping("/metrics")
    public R<PageResult<ServiceMetrics>> getMetrics(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (serviceId != null) {
            return R.ok(metricsService.queryByServiceId(serviceId, page, size));
        }
        return R.ok(metricsService.queryAll(page, size));
    }

    @GetMapping("/logs")
    public R<PageResult<ServiceLog>> getLogs(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (serviceId != null) {
            return R.ok(logService.queryByServiceId(serviceId, page, size));
        }
        return R.ok(logService.queryAll(page, size));
    }
}
