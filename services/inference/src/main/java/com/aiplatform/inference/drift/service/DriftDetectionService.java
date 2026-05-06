package com.aiplatform.inference.drift.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.drift.entity.ModelDriftReport;
import com.aiplatform.inference.drift.mapper.ModelDriftReportMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriftDetectionService {

    private final ModelDriftReportMapper driftReportMapper;

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.05");

    public ModelDriftReport checkDataDrift(Long modelId, String modelName, String modelVersion,
                                             double[] baseline, double[] current) {
        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
        double pValue = ksTest.kolmogorovSmirnovTest(baseline, current);
        double statistic = ksTest.kolmogorovSmirnovStatistic(baseline, current);

        boolean isDrifted = pValue < DEFAULT_THRESHOLD.doubleValue();

        String details = buildDataDriftDetails(baseline, current, statistic, pValue);

        ModelDriftReport report = new ModelDriftReport();
        report.setModelId(modelId);
        report.setModelName(modelName);
        report.setModelVersion(modelVersion);
        report.setDriftType("DATA_DRIFT");
        report.setStatus("COMPLETED");
        report.setDriftScore(BigDecimal.valueOf(statistic).setScale(6, RoundingMode.HALF_UP));
        report.setThreshold(DEFAULT_THRESHOLD);
        report.setIsDrifted(isDrifted);
        report.setDetails(details);
        report.setSampleSize((long) current.length);
        report.setDetectionPeriod(java.time.LocalDate.now().toString());
        driftReportMapper.insert(report);

        return report;
    }

    public ModelDriftReport checkPredictionDrift(Long modelId, String modelName, String modelVersion,
                                                   long[] baselineCounts, long[] currentCounts) {
        ChiSquareTest chiSquareTest = new ChiSquareTest();
        double[] baselineD = Arrays.stream(baselineCounts).asDoubleStream().toArray();
        double[] currentD = Arrays.stream(currentCounts).asDoubleStream().toArray();
        long[] currentL = currentCounts;
        double pValue = chiSquareTest.chiSquareTest(baselineD, currentL);
        double statistic = chiSquareTest.chiSquare(baselineD, currentL);

        boolean isDrifted = pValue < DEFAULT_THRESHOLD.doubleValue();

        String details = buildPredictionDriftDetails(baselineCounts, currentCounts, statistic, pValue);

        ModelDriftReport report = new ModelDriftReport();
        report.setModelId(modelId);
        report.setModelName(modelName);
        report.setModelVersion(modelVersion);
        report.setDriftType("PREDICTION_DRIFT");
        report.setStatus("COMPLETED");
        report.setDriftScore(BigDecimal.valueOf(statistic).setScale(6, RoundingMode.HALF_UP));
        report.setThreshold(DEFAULT_THRESHOLD);
        report.setIsDrifted(isDrifted);
        report.setDetails(details);
        report.setSampleSize(Arrays.stream(currentCounts).sum());
        report.setDetectionPeriod(java.time.LocalDate.now().toString());
        driftReportMapper.insert(report);

        return report;
    }

    public ModelDriftReport checkConceptDrift(Long modelId, String modelName, String modelVersion,
                                                double[] recentAccuracies, double thresholdAccuracy) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double acc : recentAccuracies) {
            stats.addValue(acc);
        }

        double meanAccuracy = stats.getMean();
        double stdAccuracy = stats.getStandardDeviation();
        boolean isDrifted = meanAccuracy < thresholdAccuracy;

        String details = String.format(
                "{\"meanAccuracy\":%.4f,\"stdAccuracy\":%.4f,\"thresholdAccuracy\":%.4f,\"trend\":\"%s\"}",
                meanAccuracy, stdAccuracy, thresholdAccuracy,
                isDrifted ? "DECLINING" : "STABLE");

        ModelDriftReport report = new ModelDriftReport();
        report.setModelId(modelId);
        report.setModelName(modelName);
        report.setModelVersion(modelVersion);
        report.setDriftType("CONCEPT_DRIFT");
        report.setStatus("COMPLETED");
        report.setDriftScore(BigDecimal.valueOf(meanAccuracy).setScale(6, RoundingMode.HALF_UP));
        report.setThreshold(BigDecimal.valueOf(thresholdAccuracy).setScale(4, RoundingMode.HALF_UP));
        report.setIsDrifted(isDrifted);
        report.setDetails(details);
        report.setSampleSize((long) recentAccuracies.length);
        report.setDetectionPeriod(java.time.LocalDate.now().toString());
        driftReportMapper.insert(report);

        return report;
    }

    public PageResult<ModelDriftReport> listReports(Long modelId, String driftType, int page, int size) {
        Page<ModelDriftReport> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<ModelDriftReport> wrapper = new LambdaQueryWrapper<>();
        if (modelId != null) {
            wrapper.eq(ModelDriftReport::getModelId, modelId);
        }
        if (driftType != null && !driftType.isEmpty()) {
            wrapper.eq(ModelDriftReport::getDriftType, driftType);
        }
        wrapper.orderByDesc(ModelDriftReport::getCreatedAt);
        Page<ModelDriftReport> result = driftReportMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public ModelDriftReport getLatestReport(Long modelId, String driftType) {
        LambdaQueryWrapper<ModelDriftReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDriftReport::getModelId, modelId);
        if (driftType != null && !driftType.isEmpty()) {
            wrapper.eq(ModelDriftReport::getDriftType, driftType);
        }
        wrapper.orderByDesc(ModelDriftReport::getCreatedAt);
        wrapper.last("LIMIT 1");
        return driftReportMapper.selectOne(wrapper);
    }

    public Map<String, Object> getDriftTrend(Long modelId, int limit) {
        Page<ModelDriftReport> pageParam = new Page<>(1, limit);
        Page<ModelDriftReport> result = driftReportMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ModelDriftReport>()
                        .eq(ModelDriftReport::getModelId, modelId)
                        .orderByDesc(ModelDriftReport::getCreatedAt));

        Map<String, Object> trend = new HashMap<>();
        trend.put("modelId", modelId);
        trend.put("totalReports", result.getTotal());
        trend.put("reports", result.getRecords());

        long driftedCount = result.getRecords().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsDrifted()))
                .count();
        trend.put("driftedCount", driftedCount);
        trend.put("driftRate", result.getTotal() > 0
                ? BigDecimal.valueOf((double) driftedCount / result.getTotal()).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        return trend;
    }

    private String buildDataDriftDetails(double[] baseline, double[] current, double statistic, double pValue) {
        DescriptiveStatistics baselineStats = new DescriptiveStatistics(baseline);
        DescriptiveStatistics currentStats = new DescriptiveStatistics(current);
        return String.format(
                "{\"ksStatistic\":%.6f,\"pValue\":%.6f,\"baselineMean\":%.4f,\"currentMean\":%.4f," +
                "\"baselineStd\":%.4f,\"currentStd\":%.4f}",
                statistic, pValue, baselineStats.getMean(), currentStats.getMean(),
                baselineStats.getStandardDeviation(), currentStats.getStandardDeviation());
    }

    private String buildPredictionDriftDetails(long[] baseline, long[] current, double statistic, double pValue) {
        return String.format(
                "{\"chiSquareStatistic\":%.6f,\"pValue\":%.6f,\"baselineDistribution\":%s,\"currentDistribution\":%s}",
                statistic, pValue, Arrays.toString(baseline), Arrays.toString(current));
    }
}
