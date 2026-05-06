package com.aiplatform.inference.shared;

import com.aiplatform.inference.config.K8sConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class K8sClient {

    private final K8sConfig k8sConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + k8sConfig.getToken());
        return headers;
    }

    public String getBaseUrl() {
        return k8sConfig.getApiServerUrl() + "/apis/apps/v1/namespaces/" + k8sConfig.getNamespace();
    }

    public String getCoreBaseUrl() {
        return k8sConfig.getApiServerUrl() + "/api/v1/namespaces/" + k8sConfig.getNamespace();
    }

    public String getBatchBaseUrl() {
        return k8sConfig.getApiServerUrl() + "/apis/batch/v1/namespaces/" + k8sConfig.getNamespace();
    }

    public JsonNode createDeployment(Map<String, Object> deploymentSpec) {
        try {
            String url = getBaseUrl() + "/deployments";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deploymentSpec, createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to create deployment", e);
            return null;
        }
    }

    public boolean deleteDeployment(String name) {
        try {
            String url = getBaseUrl() + "/deployments/" + name;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete deployment: {}", name, e);
            return false;
        }
    }

    public JsonNode updateDeployment(String name, Map<String, Object> deploymentSpec) {
        try {
            String url = getBaseUrl() + "/deployments/" + name;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deploymentSpec, createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to update deployment: {}", name, e);
            return null;
        }
    }

    public JsonNode getDeployment(String name) {
        try {
            String url = getBaseUrl() + "/deployments/" + name;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get deployment: {}", name, e);
            return null;
        }
    }

    public JsonNode createService(Map<String, Object> serviceSpec) {
        try {
            String url = getCoreBaseUrl() + "/services";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(serviceSpec, createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to create service", e);
            return null;
        }
    }

    public boolean deleteService(String name) {
        try {
            String url = getCoreBaseUrl() + "/services/" + name;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete service: {}", name, e);
            return false;
        }
    }

    public JsonNode createJob(Map<String, Object> jobSpec) {
        try {
            String url = getBatchBaseUrl() + "/jobs";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobSpec, createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to create job", e);
            return null;
        }
    }

    public boolean deleteJob(String name) {
        try {
            String url = getBatchBaseUrl() + "/jobs/" + name;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete job: {}", name, e);
            return false;
        }
    }

    public JsonNode getJob(String name) {
        try {
            String url = getBatchBaseUrl() + "/jobs/" + name;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get job: {}", name, e);
            return null;
        }
    }
}
