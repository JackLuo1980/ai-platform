package com.aiplatform.fastlabel.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsoleApiClient {

    @Value("${console.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listTeams() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/teams",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("data") instanceof Map<?, ?> data) {
                Object items = data.get("items");
                if (items instanceof List<?> list) {
                    return (List<Map<String, Object>>) list;
                }
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to list teams from console: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listTeamMembers(Long teamId) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/teams/" + teamId + "/members",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("data") instanceof Map<?, ?> data) {
                Object items = data.get("items");
                if (items instanceof List<?> list) {
                    return (List<Map<String, Object>>) list;
                }
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to list team members from console: {}", e.getMessage());
            return List.of();
        }
    }
}
