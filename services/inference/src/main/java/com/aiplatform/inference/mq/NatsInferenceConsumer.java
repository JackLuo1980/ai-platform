package com.aiplatform.inference.mq;

import com.aiplatform.inference.config.NatsConfig;
import com.aiplatform.inference.online.service.PredictionService;
import com.aiplatform.inference.shared.MinioService;
import com.aiplatform.inference.shared.ModelLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsInferenceConsumer {

    private final Connection natsConnection;
    private final NatsConfig natsConfig;
    private final PredictionService predictionService;
    private final ModelLoader modelLoader;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        String subject = natsConfig.getSubjectPrefix() + ".>";
        Dispatcher dispatcher = natsConnection.createDispatcher(this::onMessage);
        dispatcher.subscribe(subject);
        log.info("NATS consumer subscribed to: {}", subject);
    }

    private void onMessage(io.nats.client.Message msg) {
        try {
            String subject = msg.getSubject();
            String replyTo = msg.getReplyTo();
            byte[] data = msg.getData();

            JsonNode request = objectMapper.readTree(new String(data, StandardCharsets.UTF_8));

            String serviceIdStr = request.has("serviceId") ? request.get("serviceId").asText() : null;
            String modelPath = request.has("modelPath") ? request.get("modelPath").asText() : null;

            Map<String, Object> input = new HashMap<>();
            if (request.has("features")) {
                request.get("features").fields().forEachRemaining(entry ->
                        input.put(entry.getKey(), entry.getValue().asDouble()));
            }

            Map<String, Object> result;
            if (serviceIdStr != null && !serviceIdStr.isEmpty()) {
                result = predictionService.predict(Long.valueOf(serviceIdStr), input);
            } else if (modelPath != null && !modelPath.isEmpty()) {
                if (!modelLoader.isLoaded(modelPath)) {
                    byte[] modelData = minioService.downloadFileBytes(modelPath);
                    modelLoader.loadModel(modelPath, modelData);
                }
                result = modelLoader.predict(modelPath, input);
            } else {
                throw new IllegalArgumentException("Either serviceId or modelPath must be provided");
            }

            ObjectNode response = objectMapper.valueToTree(result);
            response.put("code", 200);
            response.put("message", "success");

            if (replyTo != null && !replyTo.isEmpty()) {
                natsConnection.publish(replyTo, response.toString().getBytes(StandardCharsets.UTF_8));
            }

            log.debug("Processed NATS message on subject: {}", subject);
        } catch (Exception e) {
            log.error("Failed to process NATS message", e);
            String replyTo = msg.getReplyTo();
            if (replyTo != null && !replyTo.isEmpty()) {
                try {
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("code", 500);
                    errorResponse.put("message", e.getMessage());
                    natsConnection.publish(replyTo, errorResponse.toString().getBytes(StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    log.error("Failed to send error response", ex);
                }
            }
        }
    }
}
