package com.aiplatform.fastlabel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aiplatform.fastlabel")
public class FastLabelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastLabelApplication.class, args);
    }
}
