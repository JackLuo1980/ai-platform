package com.aiplatform.inference.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nats")
public class NatsConfig {

    private String url;
    private String subjectPrefix;

    @Bean
    public Connection natsConnection() throws Exception {
        Options options = new Options.Builder()
                .server(url)
                .connectionTimeout(Duration.ofSeconds(5))
                .reconnectWait(Duration.ofSeconds(2))
                .maxReconnects(-1)
                .build();
        return Nats.connect(options);
    }
}
