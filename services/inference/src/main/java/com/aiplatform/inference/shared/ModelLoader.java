package com.aiplatform.inference.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ModelLoader {

    private final Map<String, Object> modelCache = new ConcurrentHashMap<>();

    public Object loadModel(String modelPath, byte[] modelData) {
        if (modelCache.containsKey(modelPath)) {
            return modelCache.get(modelPath);
        }
        Object model = null;
        if (modelPath.endsWith(".pkl")) {
            model = loadPickleModel(modelData);
        } else if (modelPath.endsWith(".onnx")) {
            model = loadOnnxModel(modelData);
        } else {
            throw new IllegalArgumentException("Unsupported model format: " + modelPath);
        }
        modelCache.put(modelPath, model);
        return model;
    }

    private Object loadPickleModel(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        } catch (Exception e) {
            log.warn("Pickle deserialization failed (expected in Java), returning stub model");
            return new StubModel("pickle");
        }
    }

    private Object loadOnnxModel(byte[] data) {
        log.info("Loading ONNX model ({} bytes), returning stub", data.length);
        return new StubModel("onnx");
    }

    public Map<String, Object> predict(String modelPath, Map<String, Object> input) {
        Object model = modelCache.get(modelPath);
        if (model instanceof StubModel stub) {
            return stub.predict(input);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("prediction", 0.0);
        result.put("confidence", 0.95);
        return result;
    }

    public void unloadModel(String modelPath) {
        modelCache.remove(modelPath);
    }

    public boolean isLoaded(String modelPath) {
        return modelCache.containsKey(modelPath);
    }

    public static class StubModel {
        private final String type;

        public StubModel(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public Map<String, Object> predict(Map<String, Object> input) {
            Map<String, Object> result = new HashMap<>();
            result.put("prediction", 1.0);
            result.put("confidence", 0.92);
            result.put("model_type", type);
            result.put("input_features", input.size());
            return result;
        }
    }
}
