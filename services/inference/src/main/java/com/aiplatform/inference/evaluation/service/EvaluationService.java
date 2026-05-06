package com.aiplatform.inference.evaluation.service;

import com.aiplatform.inference.common.PageResult;
import com.aiplatform.inference.evaluation.entity.ModelEvaluation;
import com.aiplatform.inference.evaluation.mapper.ModelEvaluationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final ModelEvaluationMapper evaluationMapper;

    public ModelEvaluation evaluateBinaryClassification(Long modelId, String modelName, String modelVersion,
                                                          double[] actualLabels, double[] predictedScores,
                                                          double threshold) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setModelName(modelName);
        evaluation.setModelVersion(modelVersion);
        evaluation.setEvaluationType("BINARY_CLASSIFICATION");
        evaluation.setTemplate("binary_classification");
        evaluation.setSampleSize(actualLabels.length);

        double auc = calculateAUC(actualLabels, predictedScores);
        evaluation.setAuc(BigDecimal.valueOf(auc).setScale(6, RoundingMode.HALF_UP));

        double ks = calculateKS(actualLabels, predictedScores);
        evaluation.setKs(BigDecimal.valueOf(ks).setScale(6, RoundingMode.HALF_UP));

        evaluation.setGini(BigDecimal.valueOf(2 * auc - 1).setScale(6, RoundingMode.HALF_UP));

        double[] predicted = Arrays.stream(predictedScores).map(s -> s >= threshold ? 1.0 : 0.0).toArray();
        double accuracy = calculateAccuracy(actualLabels, predicted);
        evaluation.setAccuracy(BigDecimal.valueOf(accuracy).setScale(6, RoundingMode.HALF_UP));

        double precision = calculatePrecision(actualLabels, predicted);
        evaluation.setPrecision(BigDecimal.valueOf(precision).setScale(6, RoundingMode.HALF_UP));

        double recall = calculateRecall(actualLabels, predicted);
        evaluation.setRecall(BigDecimal.valueOf(recall).setScale(6, RoundingMode.HALF_UP));

        double f1 = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;
        evaluation.setF1Score(BigDecimal.valueOf(f1).setScale(6, RoundingMode.HALF_UP));

        String cm = buildConfusionMatrix(actualLabels, predicted);
        evaluation.setConfusionMatrix(cm);

        String roc = buildROCCurve(actualLabels, predictedScores);
        evaluation.setRocCurve(roc);

        evaluation.setStatus("COMPLETED");
        evaluationMapper.insert(evaluation);

        return evaluation;
    }

    public ModelEvaluation evaluateMultiClass(Long modelId, String modelName, String modelVersion,
                                                int[] actualLabels, int[] predictedLabels, int numClasses) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setModelName(modelName);
        evaluation.setModelVersion(modelVersion);
        evaluation.setEvaluationType("MULTI_CLASS");
        evaluation.setTemplate("multi_class");
        evaluation.setSampleSize(actualLabels.length);

        double accuracy = calculateMulticlassAccuracy(actualLabels, predictedLabels);
        evaluation.setAccuracy(BigDecimal.valueOf(accuracy).setScale(6, RoundingMode.HALF_UP));

        double[] precisionPerClass = new double[numClasses];
        double[] recallPerClass = new double[numClasses];
        double[] f1PerClass = new double[numClasses];

        for (int c = 0; c < numClasses; c++) {
            int finalC = c;
            int tp = 0, fp = 0, fn = 0;
            for (int i = 0; i < actualLabels.length; i++) {
                if (predictedLabels[i] == finalC && actualLabels[i] == finalC) tp++;
                if (predictedLabels[i] == finalC && actualLabels[i] != finalC) fp++;
                if (predictedLabels[i] != finalC && actualLabels[i] == finalC) fn++;
            }
            precisionPerClass[c] = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
            recallPerClass[c] = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
            f1PerClass[c] = (precisionPerClass[c] + recallPerClass[c]) > 0
                    ? 2 * precisionPerClass[c] * recallPerClass[c] / (precisionPerClass[c] + recallPerClass[c]) : 0;
        }

        double macroPrecision = Arrays.stream(precisionPerClass).average().orElse(0);
        double macroRecall = Arrays.stream(recallPerClass).average().orElse(0);
        double macroF1 = Arrays.stream(f1PerClass).average().orElse(0);

        evaluation.setPrecision(BigDecimal.valueOf(macroPrecision).setScale(6, RoundingMode.HALF_UP));
        evaluation.setRecall(BigDecimal.valueOf(macroRecall).setScale(6, RoundingMode.HALF_UP));
        evaluation.setF1Score(BigDecimal.valueOf(macroF1).setScale(6, RoundingMode.HALF_UP));

        evaluation.setDetails(String.format(
                "{\"numClasses\":%d,\"macroPrecision\":%.6f,\"macroRecall\":%.6f,\"macroF1\":%.6f}",
                numClasses, macroPrecision, macroRecall, macroF1));

        evaluation.setStatus("COMPLETED");
        evaluationMapper.insert(evaluation);

        return evaluation;
    }

    public ModelEvaluation evaluateRegression(Long modelId, String modelName, String modelVersion,
                                                double[] actual, double[] predicted) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setModelName(modelName);
        evaluation.setModelVersion(modelVersion);
        evaluation.setEvaluationType("REGRESSION");
        evaluation.setTemplate("regression");
        evaluation.setSampleSize(actual.length);

        double rmse = calculateRMSE(actual, predicted);
        evaluation.setRmse(BigDecimal.valueOf(rmse).setScale(6, RoundingMode.HALF_UP));

        double mae = calculateMAE(actual, predicted);
        evaluation.setMae(BigDecimal.valueOf(mae).setScale(6, RoundingMode.HALF_UP));

        double r2 = calculateR2(actual, predicted);
        evaluation.setR2(BigDecimal.valueOf(r2).setScale(6, RoundingMode.HALF_UP));

        double mape = calculateMAPE(actual, predicted);
        evaluation.setMape(BigDecimal.valueOf(mape).setScale(6, RoundingMode.HALF_UP));

        evaluation.setStatus("COMPLETED");
        evaluationMapper.insert(evaluation);

        return evaluation;
    }

    public ModelEvaluation evaluateDetection(Long modelId, String modelName, String modelVersion,
                                               double map, double iou) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setModelName(modelName);
        evaluation.setModelVersion(modelVersion);
        evaluation.setEvaluationType("DETECTION");
        evaluation.setTemplate("detection");

        evaluation.setMap(BigDecimal.valueOf(map).setScale(6, RoundingMode.HALF_UP));
        evaluation.setIou(BigDecimal.valueOf(iou).setScale(6, RoundingMode.HALF_UP));

        evaluation.setStatus("COMPLETED");
        evaluationMapper.insert(evaluation);

        return evaluation;
    }

    public ModelEvaluation evaluateCustom(Long modelId, String modelName, String modelVersion,
                                            String scriptPath, Map<String, Object> params) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setModelName(modelName);
        evaluation.setModelVersion(modelVersion);
        evaluation.setEvaluationType("CUSTOM");
        evaluation.setTemplate("custom");
        evaluation.setStatus("PENDING");
        evaluation.setDetails("{\"scriptPath\":\"" + scriptPath + "\",\"status\":\"queued\"}");
        evaluationMapper.insert(evaluation);

        log.info("Custom evaluation queued: script={}, model={}", scriptPath, modelName);
        return evaluation;
    }

    public PageResult<ModelEvaluation> listEvaluations(Long modelId, int page, int size) {
        Page<ModelEvaluation> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<ModelEvaluation> wrapper = new LambdaQueryWrapper<>();
        if (modelId != null) {
            wrapper.eq(ModelEvaluation::getModelId, modelId);
        }
        wrapper.orderByDesc(ModelEvaluation::getCreatedAt);
        Page<ModelEvaluation> result = evaluationMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public ModelEvaluation getReport(Long id) {
        return evaluationMapper.selectById(id);
    }

    public List<Map<String, Object>> getTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

        Map<String, Object> binary = new HashMap<>();
        binary.put("name", "binary_classification");
        binary.put("label", "Binary Classification");
        binary.put("metrics", List.of("AUC", "KS", "Gini", "Accuracy", "Precision", "Recall", "F1", "Confusion Matrix", "ROC Curve"));
        templates.add(binary);

        Map<String, Object> multiClass = new HashMap<>();
        multiClass.put("name", "multi_class");
        multiClass.put("label", "Multi-Class Classification");
        multiClass.put("metrics", List.of("Accuracy", "Macro Precision", "Macro Recall", "Macro F1"));
        templates.add(multiClass);

        Map<String, Object> regression = new HashMap<>();
        regression.put("name", "regression");
        regression.put("label", "Regression");
        regression.put("metrics", List.of("RMSE", "MAE", "R2", "MAPE"));
        templates.add(regression);

        Map<String, Object> detection = new HashMap<>();
        detection.put("name", "detection");
        detection.put("label", "Object Detection");
        detection.put("metrics", List.of("mAP", "IoU"));
        templates.add(detection);

        Map<String, Object> custom = new HashMap<>();
        custom.put("name", "custom");
        custom.put("label", "Custom Evaluation");
        custom.put("metrics", List.of("Custom metrics via script"));
        templates.add(custom);

        return templates;
    }

    private double calculateAUC(double[] actual, double[] scores) {
        int n = actual.length;
        double[][] data = new double[n][2];
        for (int i = 0; i < n; i++) {
            data[i][0] = scores[i];
            data[i][1] = actual[i];
        }
        Arrays.sort(data, (a, b) -> Double.compare(b[0], a[0]));

        int posCount = (int) Arrays.stream(actual).filter(a -> a == 1.0).count();
        int negCount = n - posCount;
        if (posCount == 0 || negCount == 0) return 0.5;

        double tpr = 0, fpr = 0;
        double auc = 0;
        int tp = 0, fp = 0;

        for (double[] d : data) {
            if (d[1] == 1.0) {
                tp++;
            } else {
                fp++;
            }
            double newTpr = (double) tp / posCount;
            double newFpr = (double) fp / negCount;
            auc += (newFpr - fpr) * (newTpr + tpr) / 2;
            tpr = newTpr;
            fpr = newFpr;
        }
        return auc;
    }

    private double calculateKS(double[] actual, double[] scores) {
        int n = actual.length;
        double[][] data = new double[n][2];
        for (int i = 0; i < n; i++) {
            data[i][0] = scores[i];
            data[i][1] = actual[i];
        }
        Arrays.sort(data, (a, b) -> Double.compare(a[0], b[0]));

        int posCount = (int) Arrays.stream(actual).filter(a -> a == 1.0).count();
        int negCount = n - posCount;
        if (posCount == 0 || negCount == 0) return 0;

        double maxKS = 0;
        int posSoFar = 0, negSoFar = 0;

        for (double[] d : data) {
            if (d[1] == 1.0) posSoFar++;
            else negSoFar++;

            double cdfPos = (double) posSoFar / posCount;
            double cdfNeg = (double) negSoFar / negCount;
            maxKS = Math.max(maxKS, Math.abs(cdfPos - cdfNeg));
        }
        return maxKS;
    }

    private double calculateAccuracy(double[] actual, double[] predicted) {
        long correct = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] == predicted[i]) correct++;
        }
        return (double) correct / actual.length;
    }

    private double calculatePrecision(double[] actual, double[] predicted) {
        int tp = 0, fp = 0;
        for (int i = 0; i < actual.length; i++) {
            if (predicted[i] == 1.0 && actual[i] == 1.0) tp++;
            if (predicted[i] == 1.0 && actual[i] == 0.0) fp++;
        }
        return (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
    }

    private double calculateRecall(double[] actual, double[] predicted) {
        int tp = 0, fn = 0;
        for (int i = 0; i < actual.length; i++) {
            if (predicted[i] == 1.0 && actual[i] == 1.0) tp++;
            if (predicted[i] == 0.0 && actual[i] == 1.0) fn++;
        }
        return (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
    }

    private double calculateRMSE(double[] actual, double[] predicted) {
        double sum = 0;
        for (int i = 0; i < actual.length; i++) {
            sum += Math.pow(actual[i] - predicted[i], 2);
        }
        return Math.sqrt(sum / actual.length);
    }

    private double calculateMAE(double[] actual, double[] predicted) {
        double sum = 0;
        for (int i = 0; i < actual.length; i++) {
            sum += Math.abs(actual[i] - predicted[i]);
        }
        return sum / actual.length;
    }

    private double calculateR2(double[] actual, double[] predicted) {
        double meanActual = Arrays.stream(actual).average().orElse(0);
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < actual.length; i++) {
            ssRes += Math.pow(actual[i] - predicted[i], 2);
            ssTot += Math.pow(actual[i] - meanActual, 2);
        }
        return ssTot > 0 ? 1 - ssRes / ssTot : 0;
    }

    private double calculateMAPE(double[] actual, double[] predicted) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != 0) {
                sum += Math.abs((actual[i] - predicted[i]) / actual[i]);
                count++;
            }
        }
        return count > 0 ? sum / count * 100 : 0;
    }

    private double calculateMulticlassAccuracy(int[] actual, int[] predicted) {
        int correct = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] == predicted[i]) correct++;
        }
        return (double) correct / actual.length;
    }

    private String buildConfusionMatrix(double[] actual, double[] predicted) {
        int tp = 0, tn = 0, fp = 0, fn = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] == 1.0 && predicted[i] == 1.0) tp++;
            else if (actual[i] == 0.0 && predicted[i] == 0.0) tn++;
            else if (actual[i] == 0.0 && predicted[i] == 1.0) fp++;
            else fn++;
        }
        return String.format("{\"tp\":%d,\"tn\":%d,\"fp\":%d,\"fn\":%d}", tp, tn, fp, fn);
    }

    private String buildROCCurve(double[] actual, double[] scores) {
        int n = actual.length;
        double[][] data = new double[n][2];
        for (int i = 0; i < n; i++) {
            data[i][0] = scores[i];
            data[i][1] = actual[i];
        }
        Arrays.sort(data, (a, b) -> Double.compare(b[0], a[0]));

        int posCount = (int) Arrays.stream(actual).filter(a -> a == 1.0).count();
        int negCount = n - posCount;
        if (posCount == 0 || negCount == 0) return "[]";

        List<String> points = new ArrayList<>();
        int tp = 0, fp = 0;
        points.add(String.format("{\"fpr\":0.0,\"tpr\":0.0}"));

        for (double[] d : data) {
            if (d[1] == 1.0) tp++;
            else fp++;
            points.add(String.format("{\"fpr\":%.4f,\"tpr\":%.4f}",
                    (double) fp / negCount, (double) tp / posCount));
        }
        return "[" + String.join(",", points) + "]";
    }
}
