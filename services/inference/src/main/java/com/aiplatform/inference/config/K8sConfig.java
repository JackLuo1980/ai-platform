package com.aiplatform.inference.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "k8s")
public class K8sConfig {

    private String apiServerUrl;
    private String namespace;
    private String token;
}
