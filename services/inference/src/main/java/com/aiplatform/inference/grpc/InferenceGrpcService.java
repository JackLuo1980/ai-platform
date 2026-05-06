package com.aiplatform.inference.grpc;

import com.aiplatform.inference.online.service.PredictionService;
import com.aiplatform.inference.shared.ModelLoader;
import com.aiplatform.inference.shared.MinioService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class InferenceGrpcService extends InferenceServiceGrpc.InferenceServiceImplBase {

    private final PredictionService predictionService;
    private final ModelLoader modelLoader;
    private final MinioService minioService;
    private final Instant startTime = Instant.now();

    @Override
    public void predict(PredictRequest request, StreamObserver<PredictResponse> responseObserver) {
        long start = System.nanoTime();
        try {
            Map<String, Object> input = new HashMap<>();
            request.getFeaturesMap().forEach(input::put);

            Map<String, Object> result;
            if (!request.getServiceId().isEmpty()) {
                result = predictionService.predict(Long.valueOf(request.getServiceId()), input);
            } else if (!request.getModelPath().isEmpty()) {
                if (!modelLoader.isLoaded(request.getModelPath())) {
                    byte[] data = minioService.downloadFileBytes(request.getModelPath());
                    modelLoader.loadModel(request.getModelPath(), data);
                }
                result = modelLoader.predict(request.getModelPath(), input);
            } else {
                throw new IllegalArgumentException("Either service_id or model_path must be provided");
            }

            PredictResponse.Builder builder = PredictResponse.newBuilder()
                    .setCode(200)
                    .setMessage("success")
                    .setPrediction(result.containsKey("prediction") ? ((Number) result.get("prediction")).doubleValue() : 0.0)
                    .setConfidence(result.containsKey("confidence") ? ((Number) result.get("confidence")).doubleValue() : 0.0)
                    .setLatencyNs(System.nanoTime() - start);

            if (result.containsKey("probabilities")) {
                @SuppressWarnings("unchecked")
                Map<String, Double> probs = (Map<String, Double>) result.get("probabilities");
                probs.forEach(builder::putProbabilities);
            }

            if (request.getRawOutput() && result.containsKey("raw")) {
                builder.setRawOutput(result.get("raw").toString());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC predict failed", e);
            responseObserver.onNext(PredictResponse.newBuilder()
                    .setCode(500)
                    .setMessage(e.getMessage())
                    .setLatencyNs(System.nanoTime() - start)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<PredictRequest> streamPredict(StreamObserver<PredictResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PredictRequest request) {
                predict(request, new StreamObserver<>() {
                    @Override
                    public void onNext(PredictResponse value) {
                        responseObserver.onNext(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserver.onError(t);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                log.error("Stream error", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void healthCheck(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        long uptimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setHealthy(true)
                .setStatus("UP")
                .setUptimeMs(uptimeMs)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
