package com.aiplatform.lab.workflow.runner;

import com.aiplatform.lab.workflow.entity.WorkflowRunTask;
import com.aiplatform.lab.workflow.mapper.WorkflowRunTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRunner {

    private final WorkflowRunTaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    @Value("${mlflow.tracking-uri:http://mlflow:5000}")
    private String mlflowTrackingUri;

    @Value("${docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    @Value("${docker.enabled:false}")
    private boolean dockerEnabled;

    public void executeTask(WorkflowRunTask task) {
        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        try {
            log.info("Executing task: nodeId={}, type={}, image={}, dockerEnabled={}",
                    task.getNodeId(), task.getNodeType(), task.getImage(), dockerEnabled);

            if (dockerEnabled && task.getImage() != null && !task.getImage().isEmpty()) {
                executeDockerContainer(task);
            } else {
                simulateExecution(task);
            }
        } catch (Exception e) {
            log.error("Task execution failed: nodeId={}", task.getNodeId(), e);
            task.setStatus("FAILED");
            String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            errMsg = errMsg.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
            task.setResultJson("{\"error\":\"" + errMsg + "\"}");
        }

        task.setFinishedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void executeDockerContainer(WorkflowRunTask task) throws Exception {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        try {
            List<String> envVars = buildEnvVars(task);

            CreateContainerResponse container = dockerClient.createContainerCmd(task.getImage())
                    .withEnv(envVars)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(512L * 1024 * 1024)
                            .withCpuQuota(100000L)
                            .withCpuPeriod(100000L))
                    .withCmd("sh", "-c", getCommand(task))
                    .exec();

            String containerId = container.getId();
            log.info("Created container {} for task {}", containerId.substring(0, 12), task.getNodeId());

            dockerClient.startContainerCmd(containerId).exec();

            int exitCode = dockerClient.waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(5, java.util.concurrent.TimeUnit.MINUTES).intValue();

            String logs = collectLogs(dockerClient, containerId);

            if (exitCode == 0) {
                task.setStatus("COMPLETED");
            } else {
                task.setStatus("FAILED");
            }
            String escapedLogs = logs.substring(0, Math.min(logs.length(), 2000))
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
            task.setResultJson("{\"exitCode\":" + exitCode
                    + ",\"containerId\":\"" + containerId.substring(0, 12) + "\""
                    + ",\"logs\":\"" + escapedLogs + "\"}");

            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            } catch (Exception e) {
                log.warn("Failed to remove container {}", containerId.substring(0, 12), e);
            }
        } finally {
            try {
                httpClient.close();
            } catch (Exception ignored) {
            }
        }
    }

    private List<String> buildEnvVars(WorkflowRunTask task) {
        List<String> envVars = new ArrayList<>();
        envVars.add("MLFLOW_TRACKING_URI=" + mlflowTrackingUri);
        envVars.add("TASK_NODE_ID=" + task.getNodeId());
        envVars.add("TASK_TYPE=" + task.getNodeType());
        envVars.add("TASK_RUN_ID=" + task.getRunId());

        if (task.getParamsJson() != null) {
            try {
                JsonNode params = objectMapper.readTree(task.getParamsJson());
                Iterator<Map.Entry<String, JsonNode>> fields = params.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    envVars.add("PARAM_" + field.getKey().toUpperCase() + "=" + field.getValue().asText());
                }
            } catch (Exception e) {
                log.warn("Failed to parse params JSON", e);
            }
        }
        return envVars;
    }

    private String getCommand(WorkflowRunTask task) {
        if (task.getParamsJson() != null && task.getParamsJson().contains("command")) {
            try {
                JsonNode params = objectMapper.readTree(task.getParamsJson());
                if (params.has("command")) {
                    return params.get("command").asText();
                }
            } catch (Exception ignored) {
            }
        }
        return "echo 'Task " + task.getNodeId() + " completed'";
    }

    private String collectLogs(DockerClient dockerClient, String containerId) {
        StringBuilder sb = new StringBuilder();
        try {
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTailAll()
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            sb.append(new String(frame.getPayload()));
                        }
                    })
                    .awaitCompletion();
        } catch (Exception e) {
            log.warn("Failed to collect logs for container {}", containerId.substring(0, 12), e);
            return "Container " + containerId.substring(0, 12) + " executed (log collection failed)";
        }
        return sb.toString();
    }

    private void simulateExecution(WorkflowRunTask task) {
        StringBuilder result = new StringBuilder("{");
        result.append("\"nodeId\":\"").append(task.getNodeId()).append("\",");
        result.append("\"type\":\"").append(task.getNodeType()).append("\",");
        result.append("\"image\":\"").append(task.getImage() != null ? task.getImage() : "default").append("\",");
        result.append("\"mode\":\"simulated\",");
        result.append("\"duration_ms\":").append((long) (Math.random() * 5000 + 1000)).append(",");
        result.append("\"metrics\":{");
        if ("training".equals(task.getNodeType())) {
            result.append("\"accuracy\":").append(String.format("%.4f", 0.85 + Math.random() * 0.1)).append(",");
            result.append("\"loss\":").append(String.format("%.4f", 0.1 + Math.random() * 0.2));
        } else if ("evaluation".equals(task.getNodeType())) {
            result.append("\"auc\":").append(String.format("%.4f", 0.88 + Math.random() * 0.08)).append(",");
            result.append("\"f1\":").append(String.format("%.4f", 0.82 + Math.random() * 0.1));
        } else {
            result.append("\"status\":\"ok\"");
        }
        result.append("}}");
        task.setResultJson(result.toString());
        task.setStatus("COMPLETED");
    }
}
